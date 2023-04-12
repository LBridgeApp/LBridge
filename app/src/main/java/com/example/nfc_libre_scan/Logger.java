package com.example.nfc_libre_scan;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.util.Locale;

public class Logger {
    private final TextView loggerTextView;
    private final Activity activity;

    Logger(Activity activity, TextView loggerTextView) {
        this.loggerTextView = loggerTextView;
        this.activity = activity;
    }

    protected void inf(String log) {
        String finalLog = log.toLowerCase(Locale.ROOT);
        activity.runOnUiThread(() -> loggerTextView.append(String.format("\n%s", finalLog)));
    }

    protected void ok(String log) {
        String finalLog = log.toLowerCase(Locale.ROOT);
        activity.runOnUiThread(() -> loggerTextView.append(String.format("\nOK: %s", finalLog)));
    }

    protected void error(String log) {
        String finalLog = log.toLowerCase(Locale.ROOT);
        activity.runOnUiThread(() -> loggerTextView.append(String.format("\nERR: %s", finalLog)));
    }
}
