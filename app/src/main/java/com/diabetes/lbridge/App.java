package com.diabetes.lbridge;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;

import com.diabetes.lbridge.librelink.LibreLink;

import java.util.List;

public class App extends Application implements LibreLinkActivityListener {
    private static App instance;
    private AppDatabase appDatabase;
    public static String TAG;
    // TODO: если цель билда - релиз - проверить, что DEBUG_FLAG установлен на false
    public static final boolean DEBUG_FLAG = false;
    public static App getInstance() {
        return instance;
    }
    public AppDatabase getAppDatabase(){
        return appDatabase;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        new CriticalErrorHandler().setHandler();
        App.TAG = this.getString(R.string.app_name);
        try {
            this.appDatabase = new AppDatabase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
    @Override
    public void onTerminate() {
        appDatabase.close();
        super.onTerminate();
    }
}
