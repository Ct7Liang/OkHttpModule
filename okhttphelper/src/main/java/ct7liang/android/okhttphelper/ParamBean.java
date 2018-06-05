package ct7liang.android.okhttphelper;

import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2018-06-01.
 *
 */
class ParamBean {
    String key;
    String value;
    ParamBean(@NonNull String key, @NonNull String value){
        this.key = key;
        this.value = value;
    }
}
