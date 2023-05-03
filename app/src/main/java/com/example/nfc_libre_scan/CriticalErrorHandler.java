package com.example.nfc_libre_scan;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CriticalErrorHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;

    public CriticalErrorHandler() {
        this.context = App.getInstance().getApplicationContext();
    }

    public void setHandler() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {

        try {
            Logger.criticalError(e);
        }
        catch (Exception err){
            err.printStackTrace();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        Intent intent = new Intent(context, CriticalErrorActivity.class);
        intent.putExtra("criticalError", e);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Notification.CRITICAL_ERROR.showOrUpdate(sw.toString());

        context.startActivity(intent);

        Runtime.getRuntime().exit(1);
    }
}
