package ct7liang.android.okhttphelper;

import android.util.Log;

/**
 * Created by Administrator on 2018-05-21.
 *
 */
class LogUtils {

    static void write(String string){
        if (COkHttpUtils.isShowLog){
            Log.i(COkHttpUtils.SHOW_TAG, string);
        }
    }
}
