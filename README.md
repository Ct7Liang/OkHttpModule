### OkHttpHelper
### 使用说明

#### 1.引入
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

    dependencies {
        compile 'com.github.Ct7Liang:OkHttpModule:1.0.3'
    }
```
#### 2.初始化
```
    //初始化okHttpClient和SharedPreferences
    OkHttpHelper.init(this);

    //设置是否输出网络请求日志,默认为true
    OkHttpHelper.setShowLogEnable(true);

    //设置日志标签,默认为"okHttpHelper"
    OkHttpHelper.setLogTag("okHttpHelper");

    //设置请求是否自动管理session和session在请求头里面的键名,默认为"true","cookie"
    //请求会从响应头里面拿到session,存储在本地,自动管理表示发起请求的时候会自动携带在header里面
    OkHttpHelper.setSessionAuto(true, "cookie");
```
#### 3.使用
```
//发起请求
OkHttpHelper
    .create()
    .post()
    .desc("登录")
    .url("http://***.*****.***/***/******/*****")
    .param("account", "18*******32")
    .param("password", "123456")
    .header("header_param1", "value1")
    .header("header_param2", "value2")
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

//获取请求的时候获得的session
OkHttpHelper.getSession();   
```