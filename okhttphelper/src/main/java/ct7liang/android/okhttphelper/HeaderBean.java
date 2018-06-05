package ct7liang.android.okhttphelper;

import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2018-06-01.
 *
 */
class HeaderBean {
    String key;
    String value;
    HeaderBean(@NonNull String key, @NonNull String value){
        this.key = key;
        this.value = value;
    }
}
