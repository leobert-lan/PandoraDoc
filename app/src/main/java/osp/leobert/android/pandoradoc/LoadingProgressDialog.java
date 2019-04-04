package osp.leobert.android.pandoradoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

public final class LoadingProgressDialog extends ProgressDialog {

    private String mLoadingTip;
    private TextView titleView;

    public LoadingProgressDialog(Context context) {
        this(context, "waiting");
    }

    public LoadingProgressDialog(Context context, String content) {
        super(context, R.style.base_dialog_progress);
        this.mLoadingTip = content;
        setCanceledOnTouchOutside(true);
    }

    public LoadingProgressDialog(Context context, String content, boolean canCancel) {
        super(context, R.style.base_dialog_progress);
        this.mLoadingTip = content;
        setCanceledOnTouchOutside(canCancel);
        setCancelable(canCancel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }



    private void initData() {
    }

    public void setContent(String loadingTip) {
        mLoadingTip = loadingTip;
        if (titleView != null)
            titleView.setText(mLoadingTip);
    }

    public void setContent(int resId) {
        mLoadingTip = getContext().getString(resId);
    }

    private void initView() {
        setContentView(R.layout.base_progress_dialog);
        titleView = findViewById(R.id.base_progress_dialog_tv_msg);
        if (!TextUtils.isEmpty(mLoadingTip)) {
            titleView.setText(mLoadingTip);
        }
    }

    public String getLogTag() {
        return getClass().getSimpleName();
    }
}
