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
        compile 'com.github.Ct7Liang:OkHttpModule:1.0.0'
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

    //设置session在请求头里面的键名,默认为"cookie"
    OkHttpHelper.setSessionName("cookie");
```
#### 3.使用
```
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
```