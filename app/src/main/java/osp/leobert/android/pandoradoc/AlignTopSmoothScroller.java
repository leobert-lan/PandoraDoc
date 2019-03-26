package osp.leobert.android.pandoradoc;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;

/**
 * <p><b>Package:</b> osp.leobert.android.pandoradoc </p>
 * <p><b>Project:</b> PandoraDoc </p>
 * <p><b>Classname:</b> AlignTopSmoothScroller </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2019/3/26.
 */
public class AlignTopSmoothScroller extends LinearSmoothScroller {
    public AlignTopSmoothScroller(Context context) {
        super(context);
    }

    @Override
    protected int getHorizontalSnapPreference() {
        //Align child view's left or top with parent view's left or top
        return SNAP_TO_START;
    }

    @Override
    protected int getVerticalSnapPreference() {
        //Align child view's left or top with parent view's left or top
        return SNAP_TO_START;
    }
}
