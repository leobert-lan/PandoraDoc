package osp.leobert.android.pandoradoc;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.commonmark.node.Heading;
import org.commonmark.node.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import individual.leobert.retrofitext.RetrofitExt;
import individual.leobert.retrofitext.ext.ApiResponseHandler;
import individual.leobert.retrofitext.ext.AsyncFileResponseHandler;
import okhttp3.Headers;
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

        if (isLocalFileExist(latest)) {
            foo(latest, adapter);
        } else {
            downloadMarkdown(adapter);
        }
    }

    private void downloadMarkdown(MarkwonAdapter adapter) {
        File cacheDir = getCacheDir();
        File latest = new File(cacheDir, "AppViewHoldersReport.md");
        AsyncFileResponseHandler handler = new AsyncFileResponseHandler(latest) {
            @Override
            public void onProgress(Call<ResponseBody> call, long current, long total) {
                Log.d("lmsg", "onLoading:" + current + "/" + total);
                if (!progressDialog.isShowing())
                    mainHandler.post(() -> progressDialog.show());
            }

            @Override
            public void onResponse(Call<ResponseBody> call, File file) {
//                Log.e("lmsg", "check file md5:" + Md5.md5(file));
                loadFromFile(file, adapter);
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

    boolean isLocalFileExist(File latest) {
        return latest != null && latest.exists() && latest.isFile();
    }

    private void foo(@NonNull File latest, MarkwonAdapter adapter) {
        new Thread(() -> {
            String md5 = Md5.md5(latest);
            Log.e("lmsg", md5);
            RetrofitExt.lifeCycledWithContext(JddDocActivity.this,
                    ApiClient.getInstance().apiInstance(JddMockApi.class)
                            .latestMd5(), new ApiResponseHandler<Resp<String>>() {
                        @Override
                        public void onSuccess(int code, Call<Resp<String>> call, Headers headers, Resp<String> res) {
                            if (res.code == 0 && TextUtils.equals(res.value, md5)) {
                                loadFromFile(latest, adapter);
                            } else {
                                downloadMarkdown(adapter);

                            }
                        }

                        @Override
                        public void onFailure(int code, Call<Resp<String>> call, Headers headers, ResponseBody res) {
                            loadFromFile(latest, adapter);
                        }

                        @Override
                        public void onThrow(Throwable t) {
                            super.onThrow(t);
                            loadFromFile(latest, adapter);
                        }

                        @Override
                        public void onFinish(Call<Resp<String>> call) {
                        }
                    });
        }).start();

    }

    private void loadFromFile(File file, MarkwonAdapter adapter) {
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
    protected void onDestroy() {
        mainHandler = null;
        ApiClient.shutdown();
        super.onDestroy();
    }
}
