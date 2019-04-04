package osp.leobert.android.pandoradoc;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.commonmark.node.Heading;
import org.commonmark.node.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import individual.leobert.retrofitext.RetrofitExt;
import individual.leobert.retrofitext.ext.AsyncFileResponseHandler;
import okhttp3.ResponseBody;
import retrofit2.Call;
import ru.noties.markwon.Markwon;
import ru.noties.markwon.recycler.MarkwonAdapter;

/**
 * <p><b>Package:</b> osp.leobert.android.pandoradoc </p>
 * <p><b>Project:</b> PandoraDoc </p>
 * <p><b>Classname:</b> JddDocActivity </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2019/3/29.
 */
public class JddDocActivity extends MainActivity {
    ProgressDialog progressDialog;
    private Handler mainHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        progressDialog = new LoadingProgressDialog(this);
        mainHandler = new Handler(Looper.getMainLooper());
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void loadMarkdown(MarkwonAdapter adapter) {

        File cacheDir = getCacheDir();
        File latest = new File(cacheDir, "AppViewHoldersReport.md");
        if (!progressDialog.isShowing())
            progressDialog.show();
        AsyncFileResponseHandler handler = new AsyncFileResponseHandler(latest) {
            @Override
            public void onProgress(Call<ResponseBody> call, long current, long total) {
                Log.d("lmsg", "onLoading:" + current + "/" + total);
                if (!progressDialog.isShowing())
                    mainHandler.post(() -> progressDialog.show());
            }

            @Override
            public void onResponse(Call<ResponseBody> call, File file) {
                new Thread(() -> {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(file);
                        String content = readStream(fis);
                        final Markwon markwon = markwon(JddDocActivity.this, JddDocActivity.this::navigate2Fragment);
                        List<Node> nodes = reducer.reduce(markwon.parse(content));

                        for (int i = 0; i < nodes.size(); i++) {
                            Node node = nodes.get(i);
                            if (node instanceof Heading) {
                                String fragment = markwon.render(node).toString();
                                fragmentIndexes.put(fragment, i);
                            }
                        }

                        if (mainHandler != null)
                            mainHandler.post(
                                    () -> {
                                        if (mainHandler != null) {

                                            if (progressDialog != null)
                                                progressDialog.cancel();
                                            adapter.setParsedMarkdown(markwon, nodes);
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                            );
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }

            @Override
            public void onFailure(int code, Call<ResponseBody> call) {
                progressDialog.cancel();
            }
        };
        RetrofitExt.lifeCycledWithContext(this,
                ApiClient.getInstance().apiInstance(JddMockApi.class)
                        .download(), handler);


    }

//    private void displayContent(MarkwonAdapter adapter, String content) {
//        final Markwon markwon = markwon(this, this::navigate2Fragment);
//        List<Node> nodes = reducer.reduce(markwon.parse(content));
//
//        for (int i = 0; i < nodes.size(); i++) {
//            Node node = nodes.get(i);
//            if (node instanceof Heading) {
//                String fragment = markwon.render(node).toString();
//                fragmentIndexes.put(fragment, i);
//            }
//        }
//
//        adapter.setParsedMarkdown(markwon, nodes);
//        adapter.notifyDataSetChanged();
//    }

    @Override
    protected void onDestroy() {
        mainHandler = null;
        ApiClient.shutdown();
        super.onDestroy();
    }
}
