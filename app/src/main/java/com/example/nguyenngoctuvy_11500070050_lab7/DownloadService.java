package com.example.nguyenngoctuvy_11500070050_lab7;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.app.PendingIntent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {
    public static final String CHANNEL_ID = "DownloadChannel";
    public static final int NOTIFY_ID = 1001;

    private boolean isPaused = false;
    private boolean isCanceled = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String fileUrl = intent.getStringExtra("url");
        createNotificationChannel();
        startForeground(NOTIFY_ID, buildNotification("Bắt đầu tải..."));

        new Thread(() -> downloadFile(fileUrl)).start();
        return START_NOT_STICKY;
    }

    private void downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            int length = conn.getContentLength();
            InputStream input = new BufferedInputStream(conn.getInputStream());
            String fileName = "downloaded_file_" + System.currentTimeMillis() + ".bin";
            FileOutputStream output = new FileOutputStream(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName);

            byte[] data = new byte[1024];
            int total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCanceled) {
                    stopSelf();
                    return;
                }
                while (isPaused) {
                    Thread.sleep(500);
                }
                total += count;
                output.write(data, 0, count);

                int progress = (int) ((total * 100L) / length);
                updateNotification("Đang tải: " + progress + "%", progress);
            }

            output.flush();
            output.close();
            input.close();

            updateNotification("Hoàn tất!", 100);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification buildNotification(String text) {
        Intent pauseIntent = new Intent(this, NotificationReceiver.class);
        pauseIntent.setAction("PAUSE");
        Intent resumeIntent = new Intent(this, NotificationReceiver.class);
        resumeIntent.setAction("RESUME");
        Intent cancelIntent = new Intent(this, NotificationReceiver.class);
        cancelIntent.setAction("CANCEL");

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download Manager")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .addAction(android.R.drawable.ic_media_pause, "Pause",
                        PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(android.R.drawable.ic_media_play, "Resume",
                        PendingIntent.getBroadcast(this, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel",
                        PendingIntent.getBroadcast(this, 2, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, false)
                .build();
    }

    private void updateNotification(String text, int progress) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download Manager")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setProgress(100, progress, false)
                .build();
        manager.notify(NOTIFY_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Download Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Xử lý từ BroadcastReceiver
    public void pauseDownload() { isPaused = true; }
    public void resumeDownload() { isPaused = false; }
    public void cancelDownload() { isCanceled = true; }
}
