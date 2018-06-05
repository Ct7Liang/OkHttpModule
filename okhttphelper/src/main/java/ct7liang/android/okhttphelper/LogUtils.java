package ct7liang.android.okhttphelper;

import android.util.Log;

/**
 * Created by Administrator on 2018-05-21.
 *
 */
class LogUtils {

    static void write(String string){
        if (OkHttpHelper.isShowLog){
            Log.i(OkHttpHelper.SHOW_TAG, string);
        }
    }
}
