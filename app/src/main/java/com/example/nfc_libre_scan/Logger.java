package com.example.nfc_libre_scan;

import android.util.Log;

import java.util.Locale;

public class Logger {
    private Logger(){}
    private static OnLogListener logListener;
    private static final String TAG = "LibreTools";
    public static void setLoggerListener(OnLogListener listener){
        logListener = listener;
    }

    public static void inf(String log) {
        String finalLog = String.format("INF: %s", log.toLowerCase(Locale.ROOT));
        Log.i(TAG, finalLog);
        if(logListener != null){
            logListener.onLogReceived(finalLog);
        }
    }

    public static void ok(String log) {
        String finalLog = String.format("OK: %s", log.toLowerCase(Locale.ROOT));
        Log.i(TAG, finalLog);
        if(logListener != null){
            logListener.onLogReceived(finalLog);
        }
    }

    public static void error(String log) {
        String finalLog = String.format("ERR:\n%s", log);
        Log.e(TAG, finalLog);
        if(logListener != null){
            logListener.onLogReceived(finalLog);
        }
    }
}
