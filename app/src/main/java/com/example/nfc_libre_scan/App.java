package com.example.nfc_libre_scan;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.example.nfc_libre_scan.librelink.LibreLink;

import java.util.List;

public class App extends Application implements LibreLinkActivityListener {
    private static App instance;

    public static final String TAG = App.getInstance().getApplicationContext().getString(R.string.app_name);
    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        new CriticalErrorHandler().setHandler();
        LibreLink.setLibreLinkActivityListener(this);
    }

    @Override
    public void librelinkAppeared(boolean isActivity) {
        if(isActivity){
            ActivityManager activityManager = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
            if (activityManager != null) {
                List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
                if (!tasks.isEmpty()) {
                    ActivityManager.AppTask task = tasks.get(0);
                    task.moveToFront();
                }
            }
        }
    }

}
