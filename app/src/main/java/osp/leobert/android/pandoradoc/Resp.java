package osp.leobert.android.pandoradoc;

import android.support.annotation.Keep;

/**
 * <p><b>Package:</b> osp.leobert.android.pandoradoc </p>
 * <p><b>Project:</b> PandoraDoc </p>
 * <p><b>Classname:</b> Resp </p>
 * <p><b>Description:</b> TODO </p>
 * Created by leobert on 2019/4/4.
 */
@Keep
public class Resp<T> {
    int code;
    String msg;
    T value;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
