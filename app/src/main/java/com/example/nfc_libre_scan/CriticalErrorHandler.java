package com.example.nfc_libre_scan;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

public class CriticalErrorHandler implements Thread.UncaughtExceptionHandler{
    private final Context context;
    CriticalErrorHandler(Context context){
        this.context = context;
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Intent intent = new Intent(context, CriticalErrorActivity.class);
        intent.putExtra("criticalError", e);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        Runtime.getRuntime().exit(1);
    }
}
