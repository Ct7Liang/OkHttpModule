package ct7liang.android.okhttpmodule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import ct7liang.android.okhttphelper.OkHttpHelper;

public class MainActivity extends AppCompatActivity implements Login.OnLogin, GetUser.OnQuery {

    private OkHttpHelper login;
    private OkHttpHelper query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
                finish();
            }
        });

        login = Login.login(this);
    }

    @Override
    public void onLoginSuccess(String s) {
        Log.i("Ct7", "登录返回: " + s);
        query = GetUser.query(this);
    }

    @Override
    public void onLoginError(Exception e) {
        Log.i("Ct7", "登录失败: " + e.toString());
    }

    @Override
    public void onQuerySuccess(String s) {
        Log.i("Ct7", "获取用户信息: " +s);
    }

    @Override
    public void onQueryError(Exception e) {
        Log.i("Ct7", "获取用户信息失败: " + e.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpHelper.onDestroy(login, query);
    }
}
