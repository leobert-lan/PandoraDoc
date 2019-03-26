package osp.leobert.android.pandoradoc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import ru.noties.markwon.LinkResolverDef;
import ru.noties.markwon.MarkwonConfiguration;
import ru.noties.markwon.RenderProps;
import ru.noties.markwon.core.spans.LinkSpan;
import ru.noties.markwon.html.HtmlTag;
import ru.noties.markwon.html.tag.LinkHandler;

/**
 * <p><b>Package:</b> osp.leobert.android.pandoradoc </p>
 * <p><b>Project:</b> PandoraDoc </p>
 * <p><b>Classname:</b> PagerFragmentNavigationHandler </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2019/3/26.
 */
public class PagerFragmentNavigationHandler extends LinkHandler {

    public interface OnPagerFragmentNavigation {
        void onNavigate(@NonNull String fragment);
    }

    @NonNull
    private final OnPagerFragmentNavigation navigation;

    public PagerFragmentNavigationHandler(@NonNull OnPagerFragmentNavigation navigation) {
        this.navigation = navigation;
    }

    @Nullable
    @Override
    public Object getSpans(@NonNull MarkwonConfiguration configuration, @NonNull RenderProps renderProps, @NonNull HtmlTag tag) {
        final String destination = tag.attributes().get("href");
        if (!TextUtils.isEmpty(destination) && destination != null/*fucking lint*/) {
            String fg = destination.replace("#", "");

            return new LinkSpan(configuration.theme(), fg, new LinkResolverDef() {
                @Override
                public void resolve(View view, @NonNull String link) {
                    navigation.onNavigate(link);
                }
            });
        }
        return null;
    }
}
