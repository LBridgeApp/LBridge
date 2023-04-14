package com.example.nfc_libre_scan;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

public class Logger {
    private final TextView loggerTextView;
    private final Activity activity;

    private final String TAG = "LibreTools";

    Logger(Activity activity, TextView loggerTextView) {
        this.loggerTextView = loggerTextView;
        this.activity = activity;
    }

    public void inf(String log) {
        String finalLog = log.toLowerCase(Locale.ROOT);
        Log.i(TAG, finalLog);
        activity.runOnUiThread(() -> loggerTextView.append(String.format("\n%s", finalLog)));
    }

    public void ok(String log) {
        String finalLog = log.toLowerCase(Locale.ROOT);
        Log.i(TAG, finalLog);
        activity.runOnUiThread(() -> loggerTextView.append(String.format("\nOK: %s", finalLog)));
    }

    public void error(String log) {
        String finalLog = log.toLowerCase(Locale.ROOT);
        Log.e(TAG, finalLog);
        activity.runOnUiThread(() -> loggerTextView.append(String.format("\nERR: %s", finalLog)));
    }
}
