package com.happyplus.adbshell;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {


    private App app;
    private ADB adb;
    private static Boolean running = false;
    private Button btn;

    private EditText addressEdit;
    private EditText portEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("AdbShell", Context.MODE_PRIVATE);

        addressEdit = findViewById(R.id.address);
        String address = sharedPreferences.getString("ADDRESS", "");
        Log.d("HAPPYPLUS", address);
        if (!address.isEmpty()) {
            addressEdit.setText(address);
        }

        portEdit = findViewById(R.id.port);
        int port = sharedPreferences.getInt("PORT", 0);
        Log.d("HAPPYPLUS", Integer.toString(port));
        if (port != 0) {
            portEdit.setText(Integer.toString(port));
        }

        // 初始化ADB
        adb = new ADB(getFilesDir());

        // 连接adb
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!running) {
                    btn.setText("启动中");
                    btn.setEnabled(false);
                    SharedPreferences.Editor editor = sharedPreferences.edit();//获取编辑器
                    editor.putString("ADDRESS", addressEdit.getText().toString());
                    editor.putInt("PORT", Integer.parseInt(portEdit.getText().toString()));
                    editor.commit();//提交修改
                    start();
                } else {
                    stop();
                }
            }
        });
    }

    private void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Boolean ret = adb.adbConnect(addressEdit.getText().toString(), Integer.parseInt(portEdit.getText().toString()));
                if (ret) {
                    app = App.main(adb);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn.setText("停止");
                            running = true;
                            Toast.makeText(MainActivity.this, "服务已启动", Toast.LENGTH_LONG).show();
                            btn.setEnabled(true);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            running = false;
                            Toast.makeText(MainActivity.this, "ADB连接失败", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }

    private void stop() {
        Log.e("HAPPYPLUS", "断开adb");
        adb.disconnectAdb();
        app.stop();
        running = false;
        btn.setText("启动");
        Toast.makeText(MainActivity.this, "关闭服务", Toast.LENGTH_LONG).show();
    }

}