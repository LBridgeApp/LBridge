package com.example.nfc_libre_scan;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CriticalErrorHandler implements Thread.UncaughtExceptionHandler{
    private final Context context;
    public CriticalErrorHandler(){
        this.context = App.getInstance().getApplicationContext();
    }

    public void setHandler(){
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Logger.criticalError(e);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        Intent intent = new Intent(context, CriticalErrorActivity.class);
        intent.putExtra("criticalError", e);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Notification.CRITICAL_ERROR.update(sw.toString());

        context.startActivity(intent);
        Runtime.getRuntime().exit(1);
    }
}
