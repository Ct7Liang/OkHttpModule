package ct7liang.android.okhttphelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2018-05-25.
 *
 */
public class OkHttpUDHelper {

    private static final int POST_FILE = 0;
    private static final int POST_FILE_LIST = 1;
    //post提交格式
    private static int POST_TYPE = POST_FILE;
    private static File post_file;

    private static String cookieName = "cookie";
    private static OkHttpClient okHttpClient;
    private static SharedPreferences sp;

    /**
     * 初始化方法1, 创建OkHttpClient,避免重复创建, 创建sp文件
     */
    public static void init(Context context){
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        sp = context.getSharedPreferences("sessionID", Context.MODE_PRIVATE);
    }

    /**
     * 初始化方法2, 创建OkHttpClient,避免重复创建, 创建sp文件, 设置cookie的键名
     */
    public static void init(Context context, String cookieKeyName){
        cookieName = cookieKeyName;
        if (okHttpClient == null){
            okHttpClient = new OkHttpClient();
        }
        sp = context.getSharedPreferences("sessionID", Context.MODE_PRIVATE);
    }

    //请求路径
    private String url;
    //参数集
    private ArrayList<ParamBean> params;
    //上传文件集
    private ArrayList<FileBean> files;
    //头参数集
    private ArrayList<HeaderBean> headers;
    //发起请求方
    private Call call;

    public static OkHttpUDHelper post(){
        if (okHttpClient==null){
            throw new NullPointerException("Ct7OkHttp没有初始化: new OkHttpHelper().init()");
        }
        POST_TYPE = POST_FILE_LIST;
        return new OkHttpUDHelper();
    }

    public static OkHttpUDHelper post(@NonNull File file){
        if (okHttpClient==null){
            throw new NullPointerException("Ct7OkHttp没有初始化: new OkHttpHelper().init()");
        }
        post_file = file;
        POST_TYPE = POST_FILE;
        return new OkHttpUDHelper();
    }

    public OkHttpUDHelper url(@NonNull String string){
        this.url = string;
        return this;
    }

    public OkHttpUDHelper header(String key, String value){
        if (headers == null){
            headers = new ArrayList<>();
        }
        headers.add(new HeaderBean(key, value));
        return this;
    }

    public OkHttpUDHelper param(String key, String value){
        if (params == null){
            params = new ArrayList<>();
        }
        params.add(new ParamBean(key, value));
        return this;
    }

    public OkHttpUDHelper file(String key, String value, File file){
        if (files == null){
            files = new ArrayList<>();
        }
        files.add(new FileBean(key, value, file));
        return this;
    }

    public OkHttpUDHelper execute(final OnResponse onResponse){
        this.onResponse = onResponse;
        Request.Builder post;
        switch (POST_TYPE){
            case POST_FILE_LIST:
                post = new Request.Builder().post(getMultiparyRequestBody());
                break;
            case POST_FILE:
            default:
                MediaType fileType = MediaType.parse("File/*");
                post = new Request.Builder().post(RequestBody.create(fileType, post_file));
                break;
        }
        Request request = post
                .url(url)
                .header(cookieName, sp.getString("sessionId", ""))
                .headers(getHeaders())
                .tag(this).build();
        call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.write(System.currentTimeMillis()+"");
                Message msg = Message.obtain();
                Bundle b = new Bundle();
                b.putSerializable("exception", e);
                msg.what = 0;
                msg.setData(b);
                handler.sendMessage(msg);
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LogUtils.write(System.currentTimeMillis()+"");
                String headers = response.header("Set-Cookie");
                if (headers!=null){
                    sp.edit().putString("sessionId", headers.substring(0, headers.indexOf(';'))).apply();
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
    private MultipartBody getMultiparyRequestBody(){
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (params!=null){
            ParamBean paramBean;
            for (int i = 0; i < params.size(); i++) {
                paramBean = params.get(i);
                builder.addFormDataPart(paramBean.key, paramBean.value);
            }
        }
        if (files!=null){
            FileBean fileBean;
            for (int i = 0; i < files.size(); i++) {
                fileBean = files.get(i);
                builder.addFormDataPart(fileBean.key, fileBean.value, RequestBody.create(MediaType.parse("file/*"), fileBean.file));
            }
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