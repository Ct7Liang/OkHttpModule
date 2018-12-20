package ct7liang.android.okhttpmodule;

import android.app.Application;
import ct7liang.android.okhttphelper.OkHttpHelper;

/**
 * Created by Administrator on 2018-06-05.
 *
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpHelper.init(this);
        OkHttpHelper.setShowLogEnable(true);
        OkHttpHelper.setLogTag("okHttpHelper");
        OkHttpHelper.setSessionAuto(true, "cookie");

        OkHttpHelper.getSession();
    }
}
