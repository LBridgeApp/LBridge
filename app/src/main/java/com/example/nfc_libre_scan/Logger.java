package com.example.nfc_libre_scan;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

public class Logger {
    private final TextView loggerTextView;
    private final Activity activity;

    Logger(Activity activity, TextView loggerTextView) {
        this.loggerTextView = loggerTextView;
        this.activity = activity;
    }

    protected void inf(String log) {
        activity.runOnUiThread(() -> loggerTextView.append(String.format("%s\n", log)));
    }

    protected void ok(String log) {
        activity.runOnUiThread(() -> loggerTextView.append(String.format("OK: %s\n", log)));
    }

    protected void error(String log) {
        activity.runOnUiThread(() -> loggerTextView.append(String.format("ERR: %s\n", log)));
    }
}
