package com.example.nguyenngoctuvy_11500070050_lab7;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        DownloadService service = new DownloadService();

        if ("PAUSE".equals(action)) {
            service.pauseDownload();
        } else if ("RESUME".equals(action)) {
            service.resumeDownload();
        } else if ("CANCEL".equals(action)) {
            service.cancelDownload();
        }
    }
}
