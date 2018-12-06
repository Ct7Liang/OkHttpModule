package ct7liang.android.okhttphelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2018-05-25.
 *
 */
public class OkHttpHelper {

    private static String cookieName = "cookie";
    private static String SHOW_TAG = "okHttpHelper";
    private static boolean isShowLog = true;
    private static OkHttpClient okHttpClient;
    private static SharedPreferences sp;

    private OkHttpHelper(){}

    /**
     * 初始化okHttpClient和SharedPreferences
     * @param context Context
     */
    public static void init(Context context){
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build();
        }
        sp = context.getSharedPreferences("sessionID", Context.MODE_PRIVATE);
    }

    /**
     * 设置是否输出网络请求日志,默认为true
     * @param isShow boolean
     */
    public static void setShowLogEnable(boolean isShow){
        isShowLog = isShow;
    }

    /**
     * 设置日志标签,默认为"okHttpHelper"
     * @param tag String
     */
    public static void setLogTag(String tag){
        SHOW_TAG = tag;
    }

    /**
     * 设置session在请求头里面的键名,默认为"cookie"
     * @param sessionName String
     */
    public static void setSessionName(String sessionName){
        cookieName = sessionName;
    }

    //标记: 是否为POST方法
    private boolean isPost;
    //请求路径
    private String url;
    //请求说明
    private String desc;

    //post提交格式
    private int POST_TYPE;
    private static final int POST_STR = 0;
    private static final int POST_NONE = 1;

    private String post_string;

    //参数集
    private ArrayList<ParamBean> params;
    //头参数集
    private ArrayList<HeaderBean> headers;
    //发起请求方
    private Call call;

    /**
     * 创建OkHttpHelper对象
     * @return okHttpHelper
     */
    public static OkHttpHelper create(){
        if (okHttpClient==null){
            throw new NullPointerException("OkHttpHelper没有初始化: OkHttpHelper.init()");
        }
        return new OkHttpHelper();
    }

    public OkHttpHelper post(){
        POST_TYPE = POST_NONE;
        this.isPost = true;
        return this;
    }

    public OkHttpHelper post(@NonNull String data){
        POST_TYPE = POST_STR;
        this.post_string = data;
        this.isPost = true;
        return this;
    }

    public OkHttpHelper get(){
        this.isPost = false;
        return this;
    }

    public OkHttpHelper desc(String desc){
        this.desc = desc;
        return this;
    }

    public OkHttpHelper url(String string){
        this.url = string;
        return this;
    }

    public OkHttpHelper header(String key, String value){
        if (headers == null){
            headers = new ArrayList<>();
        }
        headers.add(new HeaderBean(key, value));
        return this;
    }

    public OkHttpHelper param(String key, String value){
        if (params == null){
            params = new ArrayList<>();
        }
        params.add(new ParamBean(key, value));
        return this;
    }

    public OkHttpHelper execute(final OnResponse onResponse){
        write(desc==null?"***************************************":"****************** " + desc + " ******************");
        this.onResponse = onResponse;
        Request request;
        if (isPost) {
            Request.Builder post;
            switch (POST_TYPE){
                case POST_STR:
                    post = new Request.Builder().post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), post_string));
                    write("请求提交参数: " + post_string);
                    break;
                case POST_NONE:
                default:
                    post = new Request.Builder().post(getRequestBody());
                    break;
            }
            request = post.url(url).headers(getHeaders()).tag(this).build();
            write("访问路径(POST): " + url);
        }else{
            request = new Request.Builder().get().headers(getHeaders()).url(url).tag(this).build();
            write("访问路径(GET): " + url);
        }
        call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Message msg = Message.obtain();
                Bundle b = new Bundle();
                write("访问异常: " + e.toString());
                b.putSerializable("exception", e);
                msg.what = 0;
                msg.setData(b);
                handler.sendMessage(msg);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String headers = response.header("Set-Cookie");
                if (headers!=null && headers.startsWith("JSESSIONID=")){
                    String substring = headers.substring(0, headers.indexOf(';'));
                    write("获取到SessionID: " + substring);
                    sp.edit().putString("sessionId", substring).apply();
                }
                ResponseBody body = response.body();
                if (body != null){
                    String string = body.string();
                    write("访问成功: " + string);
                    Message msg = Message.obtain();
                    Bundle b = new Bundle();
                    b.putCharSequence("data", string);
                    msg.what = 1;
                    msg.setData(b);
                    handler.sendMessage(msg);
                }
            }
        });
        return this;
    }

    public static void onDestroy(OkHttpHelper ... okHttpHelper) {
        for (OkHttpHelper okHttpHelper1 : okHttpHelper) {
            if (okHttpHelper1 != null) {
                okHttpHelper1.cancel();
            }
        }
    }

    /**
     * 取消请求
     */
    private void cancel(){
        if (call!=null){
            call.cancel();
        }
        if (handler!=null){
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        write(desc + "请求: 已关闭");
    }

    /**
     * 获取请求体 参数配置
     */
    private RequestBody getRequestBody(){
        FormBody.Builder builder = new FormBody.Builder();
        StringBuilder sb = new StringBuilder();
        if (params!=null){
            ParamBean paramBean;
            for (int i = 0; i < params.size(); i++) {
                paramBean = params.get(i);
                builder.add(paramBean.key, paramBean.value);
                if (i == 0){
                    sb.append("?").append(paramBean.key);
                    sb.append("=").append(paramBean.value);
                }else{
                    sb.append("&").append(paramBean.key);
                    sb.append("=").append(paramBean.value);
                }
            }
        }
        write("请求参数: " + sb);
        return builder.build();
    }

    /**
     * 获取请求头 参数配置
     */
    private Headers getHeaders(){
        Headers.Builder builder = new Headers.Builder();
        StringBuilder sb = new StringBuilder();
        if (headers!=null){
            HeaderBean headerBean;
            for (int i = 0; i < headers.size(); i++) {
                headerBean = headers.get(i);
                builder.add(headerBean.key, headerBean.value);
                sb.append(headerBean.key).append(" = ").append(headerBean.value).append("\n");
            }
        }
        String sessionId = sp.getString("sessionId", "");
        if (!sessionId.equals("")){
            builder.add(cookieName, sessionId);
            sb.append(cookieName).append(" = ").append(sessionId);
        }
        write("请求头参数: \n" + sb);
        return builder.build();
    }

    private OnResponse onResponse;
    public interface OnResponse{
        void onSuccess(String s);
        void onError(Exception e);
    }

    /**
     * handler切换线程至主线程
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (onResponse!=null){
                switch (msg.what){
                    case 0:
                        onDestroy(OkHttpHelper.this);
                        Exception exception = (Exception) msg.getData().getSerializable("exception");
                        onResponse.onError(exception);
                        break;
                    case 1:
                        onDestroy(OkHttpHelper.this);
                        onResponse.onSuccess((String) msg.getData().getCharSequence("data"));
                        break;
                }
            }
        }
    };

    private void write(String string){
        if (isShowLog){
            Log.i(SHOW_TAG, string);
        }
    }

}