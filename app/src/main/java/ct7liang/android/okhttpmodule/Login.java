package ct7liang.android.okhttpmodule;

import ct7liang.android.okhttphelper.OkHttpHelper;

/**
 * Created by Administrator on 2018-06-05.
 *
 */
public class Login {
    public static OkHttpHelper login(final OnLogin onLogin){
        return OkHttpHelper
                .post().desc("登录").url("http://bbs.52bqu.com/biz/loginc/login")
                .param("account", "18736607332")
                .param("password", "123456")
                .execute(new OkHttpHelper.OnResponse() {
                    @Override
                    public void onSuccess(String s) {
                        onLogin.onLoginSuccess(s);
                    }
                    @Override
                    public void onError(Exception e) {
                        onLogin.onLoginError(e);
                    }
                });
    }
    public interface OnLogin{
        void onLoginSuccess(String s);
        void onLoginError(Exception e);
    }
}