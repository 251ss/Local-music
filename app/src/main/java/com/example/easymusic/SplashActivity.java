package com.example.easymusic;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_DISPLAY_LENGHT= 1500;    //延迟1.5秒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //rxPermissions:处理权限申请的库
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE)
                .subscribe(granded -> {
                    if (granded) {
                        //获取权限后
                        new Handler().postDelayed(new Runnable() {
                            public void run() {
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);	//第二个参数即为执行完跳转后的Activity
                                startActivity(intent);
                                SplashActivity.this.finish();   //关闭splashActivity，将其回收，否则按返回键会返回此界面
                            }
                        }, SPLASH_DISPLAY_LENGHT);
                        Log.d("rxRermision", "get");
                    } else {
                        Toast.makeText(this, "未获取到存储权限", Toast.LENGTH_LONG).show();
                        Log.d("rxRermision", "denied");
                    }
                });


    }
}
