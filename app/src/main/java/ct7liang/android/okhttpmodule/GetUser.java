package ct7liang.android.okhttpmodule;

import ct7liang.android.okhttphelper.OkHttpHelper;

/**
 * Created by Administrator on 2018-06-05.
 *
 */
public class GetUser {
    public static OkHttpHelper query(final OnQuery onQuery){
        return OkHttpHelper
                .post().desc("获取用户信息").url("http://bbs.52bqu.com/biz/userc/appGetUser")
                .execute(new OkHttpHelper.OnResponse() {
                    @Override
                    public void onSuccess(String s) {
                        onQuery.onQuerySuccess(s);
                    }
                    @Override
                    public void onError(Exception e) {
                        onQuery.onQueryError(e);
                    }
                });
    }
    public interface OnQuery{
        void onQuerySuccess(String s);
        void onQueryError(Exception e);
    }
}