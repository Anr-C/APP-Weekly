package com.lckiss.weekly.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import com.lckiss.weekly.R;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.lckiss.weekly.util.HttpUtil.getFileName;
import static com.lckiss.weekly.util.ResUtil.installApp;
import static com.lckiss.weekly.widget.MyUtils.showToast;

public class AutoUpdateService extends IntentService {
    private NotificationManager mNotificationManager;
    private Notification.Builder builder;
    private int length;
    private File apkFile;
    private MyBroadcastReceiver myBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra("progress", 0);
            updateNotification(progress);
        }
    }

    public AutoUpdateService() {
        super("AutoUpdateService Service");
    }

    private void updateNotification(int progress) {
        if (progress == length) {
            Intent installIntent = installApp(AutoUpdateService.this, apkFile);
            PendingIntent intent = PendingIntent.getActivity(AutoUpdateService.this, 0, installIntent, 0);
            builder.setContentText("下载完成");
            builder.setContentIntent(intent);
            mNotificationManager.cancel(1);
            mNotificationManager.notify(1, builder.build());
            localBroadcastManager.unregisterReceiver(myBroadcastReceiver);
            startActivity(installIntent);
        } else if (progress < 0) {
            showToast(AutoUpdateService.this, "文件损坏");
        } else {
            builder.setContentText("下载进度:" + (int) ((progress * 1.0 / length) * 100) + "%").setProgress(length, progress, false);
            mNotificationManager.notify(1, builder.build());
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        registerBroadcast();
        Intent downloadIntent = new Intent();
        //定义广播的事件类型
        downloadIntent.setAction("com.lckiss.weekly.DOWNLOAD");
        try {
            String path = intent.getStringExtra("url");
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            //获取文件大小
            long contentLength = conn.getContentLength();
            if (contentLength <= 0) {
                downloadIntent.putExtra("progress", -1);
                localBroadcastManager.sendBroadcast(downloadIntent);
                return;
            }
            length = (int) contentLength;
            showNotification();
            //本地创建文件类型
            String filename = getFileName(conn, path);
            File extDir = Environment.getExternalStorageDirectory();
            apkFile = new File(extDir + "/Weekly/" + filename);
            if (apkFile.exists()) {
               apkFile.delete();
            } else {
                apkFile.getParentFile().mkdir();
            }
            RandomAccessFile raf = new RandomAccessFile(apkFile, "rwd");
            //设置文件大小
            raf.setLength(contentLength);
            //将数据写到raf中
            InputStream is = conn.getInputStream();
            int len;
            int pos = 0;
            int current = 0;
            //3kb缓存
            byte[] buffer = new byte[3 * 1024];
            while ((len = is.read(buffer)) != -1) {
                raf.write(buffer, 0, len);
                current += len;
                if (pos == 10) {
                    pos = 0;
                    downloadIntent.putExtra("progress", current);
                    localBroadcastManager.sendBroadcast(downloadIntent);
                }
                pos++;
            }
            downloadIntent.putExtra("progress", current);
            localBroadcastManager.sendBroadcast(downloadIntent);
            is.close();
            raf.close();
        } catch (Exception e) {
            showToast(AutoUpdateService.this, "文件下载异常,稍后再试");
        }
    }

    private void registerBroadcast() {
        localBroadcastManager = LocalBroadcastManager.getInstance(AutoUpdateService.this);
        //new出上边定义好的BroadcastReceiver
        myBroadcastReceiver = new MyBroadcastReceiver();
        //实例化过滤器并设置要过滤的广播
        IntentFilter intentFilter = new IntentFilter("com.lckiss.weekly.DOWNLOAD");
        //注册广播
        localBroadcastManager.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    private void showNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(this);
        builder.setContentTitle("Weekly更新服务");
        builder.setSmallIcon(R.mipmap.new_logo);
        builder.setAutoCancel(false);
    }

}
