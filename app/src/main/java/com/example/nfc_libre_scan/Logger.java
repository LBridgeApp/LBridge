package com.example.nfc_libre_scan;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private Logger(){}
    private static LogListener logListener;
    private static final String TAG = "LibreTools";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    public static void setLoggerListener(LogListener listener){
        logListener = listener;
    }

    // TODO: рассмотреть идею писать логи в базу данных
    public static void inf(String log) {
        String finalLog = String.format("[INF] [%s] %s", LocalDateTime.now().format(formatter), log);
        Log.i(TAG, finalLog);
        if(logListener != null){
            logListener.logReceived(finalLog);
        }
    }

    public static void ok(String log) {
        String finalLog = String.format("[OK] [%s] %s", LocalDateTime.now().format(formatter), log);
        Log.i(TAG, finalLog);
        if(logListener != null){
            logListener.logReceived(finalLog);
        }
    }

    public static void error(String log) {
        String finalLog = String.format("[ERR] [%s] %s", LocalDateTime.now().format(formatter), log);
        Log.e(TAG, finalLog);
        if(logListener != null){
            logListener.logReceived(finalLog);
        }
    }

    public static void error(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw); // to PrintWriter
        e.printStackTrace(); // to console
        Logger.error(sw.toString()); // to main activity logger window
    }
}
