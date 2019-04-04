package osp.leobert.android.pandoradoc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.Toast;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ru.noties.markwon.AbstractMarkwonPlugin;
import ru.noties.markwon.Markwon;
import ru.noties.markwon.MarkwonConfiguration;
import ru.noties.markwon.MarkwonReducer;
import ru.noties.markwon.MarkwonVisitor;
import ru.noties.markwon.core.CorePlugin;
import ru.noties.markwon.html.HtmlPlugin;
import ru.noties.markwon.html.MarkwonHtmlRenderer;
import ru.noties.markwon.image.ImagesPlugin;
import ru.noties.markwon.image.svg.SvgPlugin;
import ru.noties.markwon.recycler.MarkwonAdapter;
import ru.noties.markwon.recycler.SimpleEntry;
import ru.noties.markwon.recycler.table.TableEntry;
import ru.noties.markwon.recycler.table.TableEntryPlugin;
import ru.noties.markwon.urlprocessor.UrlProcessor;
import ru.noties.markwon.urlprocessor.UrlProcessorRelativeToAbsolute;

public class MainActivity extends AppCompatActivity {

    protected final MarkwonReducer reducer = MarkwonReducer.directChildren();

    //没有仔细阅读Markwon内部代码，应该有更加合适的方式得到其index，目前方法很粗糙，暂且使用着
    protected final HashMap<String, Integer> fragmentIndexes = new HashMap<>();
    private LinearSmoothScroller alignTopScroller;

    RecyclerView recyclerView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);
        alignTopScroller = new AlignTopSmoothScroller(this);

        // create MarkwonAdapter and register two blocks that will be rendered differently
        // * fenced code block (can also specify the same Entry for indended code block)
        // * table block
        final MarkwonAdapter adapter = MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                // we can simply use bundled SimpleEntry
                .include(FencedCodeBlock.class, SimpleEntry.create(R.layout.adapter_fenced_code_block, R.id.text))
                .include(TableBlock.class, TableEntry.create(builder -> builder
                        .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                        .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                .build();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            alignTopScroller.setTargetPosition(0);

            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager != null)
                layoutManager.startSmoothScroll(alignTopScroller);
        });

        loadMarkdown(adapter);

        // please note that we should notify updates (adapter doesn't do it implicitly)
        adapter.notifyDataSetChanged();
    }

    protected void loadMarkdown(MarkwonAdapter adapter) {
        final Markwon markwon = markwon(this, this::navigate2Fragment);

        String content = loadReadMe(this);
        List<Node> nodes = reducer.reduce(markwon.parse(content));

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (node instanceof Heading) {
                String fragment = markwon.render(node).toString();
                fragmentIndexes.put(fragment, i);
            }
        }

        adapter.setParsedMarkdown(markwon, nodes);
    }

    @NonNull
    protected Markwon markwon(@NonNull Context context, final PagerFragmentNavigationHandler.OnPagerFragmentNavigation onPagerFragmentNavigation) {
        return Markwon.builder(context)
                .usePlugin(CorePlugin.create())
                .usePlugin(ImagesPlugin.createWithAssets(context))
                .usePlugin(SvgPlugin.create(context.getResources()))
                // important to use TableEntryPlugin instead of TablePlugin
                .usePlugin(TableEntryPlugin.create(context))
//                .usePlugin(HtmlPlugin.create())
                .usePlugin(new HtmlPlugin() {
                    @Override
                    public void configureHtmlRenderer(@NonNull MarkwonHtmlRenderer.Builder builder) {
                        super.configureHtmlRenderer(builder);
                        builder.setHandler(
                                Arrays.asList("a", "fg"), new PagerFragmentNavigationHandler(onPagerFragmentNavigation));
                    }
                })
//                .usePlugin(SyntaxHighlightPlugin.create())
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.urlProcessor(new UrlProcessorInitialReadme());
                    }

                    @Override
                    public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
                        builder.on(FencedCodeBlock.class, (visitor, fencedCodeBlock) -> {
                            // we actually won't be applying code spans here, as our custom view will
                            // draw background and apply mono typeface
                            //
                            // NB the `trim` operation on literal (as code will have a new line at the end)
                            final CharSequence code = visitor.configuration()
                                    .syntaxHighlight()
                                    .highlight(fencedCodeBlock.getInfo(), fencedCodeBlock.getLiteral().trim());
                            visitor.builder().append(code);
                        });
                    }
                })
                .build();
    }

    void navigate2Fragment(@NonNull String fragment) {
        if (fragmentIndexes.containsKey(fragment)) {
            Integer i = fragmentIndexes.get(fragment);
            if (i != null) {
                alignTopScroller.setTargetPosition(i);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null)
                    layoutManager.startSmoothScroll(alignTopScroller);
                return;
            }
        }
        Toast.makeText(this, "cannot find: " + fragment, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    protected String loadReadMe(@NonNull Context context) {
        InputStream stream = null;
        try {
            stream = context.getAssets().open("AppViewHoldersReport.md");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readStream(stream);
    }

    @NonNull
    protected String readStream(@Nullable InputStream inputStream) {

        String out = null;

        if (inputStream != null) {
            BufferedReader reader = null;
            //noinspection TryFinallyCanBeTryWithResources
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                final StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line)
                            .append('\n');
                }
                out = builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // no op
                    }
                }
            }
        }

        if (out == null) {
            throw new RuntimeException("Cannot read stream");
        }

        return out;
    }

    private static class UrlProcessorInitialReadme implements UrlProcessor {

        private static final String GITHUB_BASE = "https://github.com/noties/Markwon/raw/master/";

        private final UrlProcessorRelativeToAbsolute processor
                = new UrlProcessorRelativeToAbsolute(GITHUB_BASE);

        @NonNull
        @Override
        public String process(@NonNull String destination) {
            String out;
            final Uri uri = Uri.parse(destination);
            if (TextUtils.isEmpty(uri.getScheme())) {
                out = processor.process(destination);
            } else {
                out = destination;
            }
            return out;
        }
    }
}
