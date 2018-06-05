package ct7liang.android.okhttphelper;

import android.support.annotation.NonNull;
import java.io.File;

/**
 * Created by Administrator on 2018-06-04.
 *
 */
class FileBean {
    String key;
    String value;
    File file;
    FileBean(@NonNull String key, @NonNull String value, @NonNull File file){
        this.key = key;
        this.value = value;
        this.file = file;
    }
}