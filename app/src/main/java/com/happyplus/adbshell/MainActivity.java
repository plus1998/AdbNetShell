package com.happyplus.adbshell;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "HAPPYPLUS";

    private Button btn;

    private EditText addressEdit;
    private EditText portEdit;

    private Intent foregroundIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("AdbShell", Context.MODE_PRIVATE);

        // 前台服务
        foregroundIntent = new Intent(MainActivity.this, ForegroundService.class);

        addressEdit = findViewById(R.id.address);
        String address = sharedPreferences.getString("ADDRESS", "");
        Log.d(TAG, address);
        if (!address.isEmpty()) {
            addressEdit.setText(address);
        }

        portEdit = findViewById(R.id.port);
        int port = sharedPreferences.getInt("PORT", 0);
        Log.d(TAG, Integer.toString(port));
        if (port != 0) {
            portEdit.setText(Integer.toString(port));
        }

        // 连接adb
        btn = findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ForegroundService.STATUS.equals("STOP")) {
                    btn.setText("启动中");
                    btn.setEnabled(false);
                    String ADDRESS = addressEdit.getText().toString();
                    int PORT = Integer.parseInt(portEdit.getText().toString());
                    // 保存配置
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("ADDRESS", ADDRESS);
                    editor.putInt("PORT", PORT);
                    editor.commit();
                    // 传参给前台服务
                    foregroundIntent.putExtra("ADDRESS", ADDRESS);
                    foregroundIntent.putExtra("PORT", PORT);
                    foregroundIntent.setAction("RUN");
                } else {
                    foregroundIntent.setAction("STOP");
                    btn.setText("停止中");
                    btn.setEnabled(false);
                }
                // 启动
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(foregroundIntent);
                } else {
                    startService(foregroundIntent);
                }
            }
        });

        // 获取服务状态
        Log.e(TAG, "ForeService:" + ForegroundService.STATUS);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refreshStatus();
                        }
                    });
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void refreshStatus() {
        if (ForegroundService.STATUS.equals("RUNNING")) {
            btn.setText("停止");
            btn.setEnabled(true);
        }
        if (ForegroundService.STATUS.equals("STOP")) {
            btn.setText("启动");
            btn.setEnabled(true);
        }
        if (ForegroundService.STATUS.equals("START")) {
            btn.setText("启动中");
            btn.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}