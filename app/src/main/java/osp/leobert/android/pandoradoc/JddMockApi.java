package osp.leobert.android.pandoradoc;

import individual.leobert.retrofitext.core.ApiDef;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;

/**
 * <p><b>Package:</b> osp.leobert.android.pandoradoc </p>
 * <p><b>Project:</b> PandoraDoc </p>
 * <p><b>Classname:</b> JddMockApi </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2019/3/29.
 */
@ApiDef
public interface JddMockApi {

    @GET("/pandoradoc/latest")
    @Streaming
    Call<ResponseBody> download();
}
