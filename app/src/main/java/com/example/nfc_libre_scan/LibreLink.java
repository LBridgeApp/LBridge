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
import com.example.nfc_libre_scan.librelink_db.SqliteSequence;

import java.io.IOException;

public class LibreLink implements LibreMessageCallback, View.OnClickListener {

    private final Logger logger;
    private LibreMessage libreMessage;
    private final Activity activity;
    private final RootLib rootLib;
    @SuppressLint("SdCardPath")
    private final String libreLinkDbPath = "/data/data/com.freestylelibre.app.ru/files/sas.db";
    private final String ourDbPath;

    LibreLink(Activity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
        this.rootLib = new RootLib(activity, logger);
        this.ourDbPath = activity.getDatabasePath("sas.db").getAbsolutePath();
    }

    public void listenBtnClicks() {
        Button sugarAddingBtn = activity.findViewById(R.id.sugarAddingBtn);
        Button databaseRemovingBtn = activity.findViewById(R.id.removeLibrelinkDB);
        sugarAddingBtn.setOnClickListener(this);
        databaseRemovingBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sugarAddingBtn || v.getId() == R.id.removeLibrelinkDB) {
            boolean granted = rootLib.requestRoot();
            if (!granted) {
                logger.error("Root is not granted.");
                return;
            } else {
                logger.ok("Root granted");
            }
        }

        if (v.getId() == R.id.sugarAddingBtn) {
            addLastScanToDatabase();
        }
        if (v.getId() == R.id.removeLibrelinkDB) {
            removeLibreLinkDatabase();
        }
    }

    public void addLastScanToDatabase() {
        if (libreMessage == null) {
            logger.error("App does not have any libre scans");
            return;
        }

        killLibreLink();
        try {
            rootLib.copyFile(libreLinkDbPath, ourDbPath);
            logger.ok("db to our app copied");
        } catch (IOException e) {
            logger.error("Failed to copy db to our app");
            return;
        }

        try {
            rootLib.setFilePermission(ourDbPath, 666);
            logger.ok("Permission 666 set to db in our app");
        } catch (IOException e) {
            logger.error("Failed to set 666 permission to db in our app");
            return;
        }

        try {
            editDatabase();
            logger.ok("db edited");
        } catch (IOException e) {
            logger.error("Failed to edit db in our app");
            return;
        }

        try {
            rootLib.copyFile(ourDbPath, libreLinkDbPath);
            logger.ok("edited db to librelink app copied!");
        } catch (IOException e) {
            logger.error("Failed to copy db to librelink app");
            return;
        }

        try {
            rootLib.setFilePermission(libreLinkDbPath, 660);
            logger.ok("permission 660 set to db in librelink app");
        } catch (IOException e) {
            logger.error("Failed to set 660 permission to db in librelink app");
            return;
        }
        startLibreLink();
    }

    public void removeLibreLinkDatabase() {
        killLibreLink();
        try {
            rootLib.removeFile(libreLinkDbPath);
            logger.ok("LibreLink db removed");
        } catch (IOException e) {
            logger.error("Failed to remove librelink db");
        }
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
        logger.ok("RawScan record done.");

        SqliteSequence sqliteSequenceRecord = new SqliteSequence(db);
        sqliteSequenceRecord.updateItselfInDb();
        logger.ok("sqlite_sequence table updated.");

        db.close();
        logger.ok("database closed");
    }

    @Override
    public void onLibreMessageReceived(LibreMessage libreMessage) {
        logger.ok("New LibreMessage received.");
        this.libreMessage = libreMessage;
    }
}
