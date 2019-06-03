package osp.leobert.android.vh.reporter;


import android.support.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p><b>Package:</b> osp.leobert.android.vh.reporter </p>
 * <p><b>Classname:</b> ViewHolder </p>
 * <p><b>Description:</b> notate ViewHolder impl and generate doc </p>
 * Created by leobert on 2018/10/23.
 */
@Target({ElementType.TYPE})
public @interface ViewHolder {
    @Usage
    String[] usage() /*default Detail*/;

    String alias() default "";

    int[] version() default 1;

    /**
     * @return 以后有必要就加上sample图
     */
    String[] pic() default {};

    String Global = "全局通用";
    String Index = "Index *首页*";

    @StringDef({
            Global,
            Index
    })
    @interface Usage {

    }
}
