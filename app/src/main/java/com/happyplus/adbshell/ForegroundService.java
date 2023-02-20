package com.happyplus.adbshell;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service {

    private static final String TAG = "HAPPYPLUS";
    public static String STATUS = "STOP";

    private App app;
    private ADB adb;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "前台服务 onCreate");
        // 初始化ADB
        adb = new ADB(getFilesDir());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        // 创建通知
        Notification notification = createForegroundNotification();
        int NOTIFICATION_ID = 8999;
        startForeground(NOTIFICATION_ID, notification);
        // 标记服务启动
        ForegroundService.STATUS = "START";
        // 数据获取
        String ADDRESS = intent.getStringExtra("ADDRESS");
        int PORT = intent.getIntExtra("PORT", 5555);
        // 开关
        String action = intent.getAction();
        Log.d(TAG, "action: " + action);
        if (action.equals("RUN")) {
            // 启动
            Log.d(TAG, "前台服务 连接参数: " + ADDRESS + ":" + PORT);
            startServer(ADDRESS, PORT);
        } else if (action.equals("STOP")) {
            // 关闭
            stopServer();
            stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    private Notification createForegroundNotification() {
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 123, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        //前台通知显示
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "adb_net_shell_channel";//自定义字符串
        String name = "AdbShell启动前台服务";//频道名称
        String description = "ADB shell net server";//通道描述,界面不显示
        String title = description + "运行中";
        String text = "点击查看详情";
        int importance = 0;//优先级
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            importance = NotificationManager.IMPORTANCE_HIGH;
        }
        NotificationCompat.Builder notification = null;
        //Android8.0之后和之前的通知有很大的差异
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = null;
            channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            manager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this, id)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)//图标
                    .setContentTitle(title)
                    .setContentText(text)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent);
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent);
        }
        return notification.build();
    }

    private void startServer(String ADDRESS, int PORT) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Boolean ret = adb.adbConnect(ADDRESS, PORT);
                if (ret) {
                    app = App.main(adb);
                    ForegroundService.STATUS = "RUNNING";
                } else {
                    Toast.makeText(getApplicationContext(), "ADB连接失败", Toast.LENGTH_LONG).show();
                    ForegroundService.STATUS = "STOP";
                }
            }
        }).start();
    }

    private void stopServer() {
        Log.e(TAG, "断开adb");
        ForegroundService.STATUS = "STOP";
        adb.disconnectAdb();
        app.stop();
        Toast.makeText(getApplicationContext(), "关闭服务", Toast.LENGTH_LONG).show();
    }
}
