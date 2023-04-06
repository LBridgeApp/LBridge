package com.example.nfc_libre_scan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.Button;

import com.example.nfc_libre_scan.librelink_db.RawScan;

import java.io.IOException;

public class LibreLinkPatcher implements View.OnClickListener, LibreMessageCallback {

    private final Logger logger;
    private LibreMessage libreMessage;
    private final Activity activity;
    private final Button libreLinkPatcherBtn;
    private final RootLib rootLib;
    @SuppressLint("SdCardPath")
    private final String libreLinkDbPath = "/data/data/com.freestylelibre.app.ru/files/sas.db";
    private final String ourDbPath;

    LibreLinkPatcher(Activity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
        this.libreLinkPatcherBtn = activity.findViewById(R.id.librelinkBtn);
        this.rootLib = new RootLib(activity, logger);
        this.ourDbPath = activity.getDatabasePath("sas.db").getAbsolutePath();
    }

    public void listen() {
        libreLinkPatcherBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (libreMessage == null) {
            logger.error("LibreMessage is empty yet. Operation refused.");
            return;
        }

        boolean rootIsGranted = rootLib.requestRootedProcess();
        if (!rootIsGranted) {
            logger.error("Root is not granted.");
            return;
        } else {
            logger.ok("Root granted");
        }

        try {
            rootLib.copyFile(libreLinkDbPath, ourDbPath);
            rootLib.setFilePermission(ourDbPath, 666);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

        try {
            editDatabase();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }

        killLibreLink();
        try {
            rootLib.copyFile(ourDbPath, libreLinkDbPath);
            rootLib.setFilePermission(libreLinkDbPath, 660);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        startLibreLink();
    }

    private void startLibreLink() {
        final String packageName = "com.freestylelibre.app.ru";
        final String activityName = "com.librelink.app.ui.SplashActivity";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        activity.startActivity(intent);
        logger.ok("LibreLink started");
    }

    private void killLibreLink() {
        final String packageName = "com.freestylelibre.app.ru";
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
        logger.ok("LibreLink killed");
    }

    private void editDatabase() throws IOException {
        byte[] patchInfo = libreMessage.getPatchInfo();
        byte[] payload = libreMessage.getPayload();

        SQLiteDatabase db = SQLiteDatabase.openDatabase(activity.getDatabasePath("sas.db").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        logger.ok("database opened. Trying to write...");
        RawScan rawScanRecord = new RawScan(db, patchInfo, payload);
        rawScanRecord.writeItselfInDB();
        logger.ok("RawScan record done. Closing database...");
        db.close();
        logger.ok("database closed");
    }

    @Override
    public void onLibreMessageReceived(LibreMessage libreMessage) {
        logger.ok("New LibreMessage received.");
        this.libreMessage = libreMessage;
    }
}
