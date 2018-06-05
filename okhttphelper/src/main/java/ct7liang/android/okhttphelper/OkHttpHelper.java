package ct7liang.android.okhttphelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import java.io.IOException;
import java.util.ArrayList;
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
    private static OkHttpClient okHttpClient;
    private static SharedPreferences sp;
    public static boolean isShowLog = true;
    public static String SHOW_TAG = "ct7";

    /**
     * 初始化方法1, 创建OkHttpClient,避免重复创建, 创建sp文件
     */
    public static void init(Context context, String logTag, boolean showLog){
        isShowLog = showLog;
        SHOW_TAG = logTag;
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        sp = context.getSharedPreferences("sessionID", Context.MODE_PRIVATE);
    }

    /**
     * 初始化方法2, 创建OkHttpClient,避免重复创建, 创建sp文件, 设置cookie的键名
     */
    public static void init(Context context, String cookieKeyName, String logTag, boolean showLog){
        cookieName = cookieKeyName;
        isShowLog = showLog;
        SHOW_TAG = logTag;
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        sp = context.getSharedPreferences("sessionID", Context.MODE_PRIVATE);
    }

    //标记: 是否为POST方法
    private boolean isPost;
    //请求路径
    private String url;
    //请求说明
    private String desc;

    //post提交格式
    private static int POST_TYPE;
    private static final int POST_STR = 0;
    private static final int POST_NONE = 1;

    private static String post_string;

    //参数集
    private ArrayList<ParamBean> params;
    //头参数集
    private ArrayList<HeaderBean> headers;
    //发起请求方
    private Call call;

    public static OkHttpHelper post(){
        if (okHttpClient==null){
            throw new NullPointerException("Ct7OkHttp没有初始化: new OkHttpHelper().init()");
        }
        POST_TYPE = POST_NONE;
        OkHttpHelper cOkHttpUtils = new OkHttpHelper();
        cOkHttpUtils.isPost = true;
        return cOkHttpUtils;
    }

    public static OkHttpHelper post(@NonNull String data){
        if (okHttpClient==null){
            throw new NullPointerException("Ct7OkHttp没有初始化: new OkHttpHelper().init()");
        }
        POST_TYPE = POST_STR;
        post_string = data;
        OkHttpHelper cOkHttpUtils = new OkHttpHelper();
        cOkHttpUtils.isPost = true;
        return cOkHttpUtils;
    }

    public static OkHttpHelper get(){
        if (okHttpClient==null){
            throw new NullPointerException("Ct7OkHttp没有初始化: new OkHttpHelper().init()");
        }
        OkHttpHelper cOkHttpUtils = new OkHttpHelper();
        cOkHttpUtils.isPost = false;
        return cOkHttpUtils;
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
        LogUtils.write(desc==null?"------------------------":"--------- " + desc + " ---------");
        this.onResponse = onResponse;
        Request request;
        if (isPost) {
            Request.Builder post;
            switch (POST_TYPE){
                case POST_STR:
                    post = new Request.Builder().post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), post_string));
                    LogUtils.write("参数: " + post_string);
                    break;
                case POST_NONE:
                default:
                    post = new Request.Builder().post(getRequestBody());
                    break;
            }
            request = post.url(url).headers(getHeaders()).tag(this).build();
            LogUtils.write("访问路径: " + url);
        }else{
            request = new Request.Builder().get().headers(getHeaders()).url(url).tag(this).build();
            LogUtils.write("访问路径: " + url);
        }
        call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Message msg = Message.obtain();
                Bundle b = new Bundle();
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
                    LogUtils.write("获取到SessionID: " + substring);
                    sp.edit().putString("sessionId", substring).apply();
                }
                ResponseBody body = response.body();
                if (body != null){
                    String string = body.string();
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

    /**
     * 取消请求
     */
    public void cancel(){
        if (call.isExecuted() && !call.isCanceled()){
            call.cancel();
        }
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
            LogUtils.write("参数: " + sb);
        }
        return builder.build();
    }

    /**
     * 获取请求头 参数配置
     */
    private Headers getHeaders(){
        Headers.Builder builder = new Headers.Builder();
        if (headers!=null){
            HeaderBean headerBean;
            for (int i = 0; i < headers.size(); i++) {
                headerBean = headers.get(i);
                builder.add(headerBean.key, headerBean.value);
            }
        }
        String sessionId = sp.getString("sessionId", "");
        if (!sessionId.equals("")){
            builder.add(cookieName, sessionId);
            LogUtils.write("携带sessionID: " + sessionId);
        }
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
                        onResponse.onError((Exception) msg.getData().getParcelable("exception"));
                        break;
                    case 1:
                        onResponse.onSuccess((String) msg.getData().getCharSequence("data"));
                        break;
                }
            }
        }
    };

}