package com.example.nfc_libre_scan;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.example.nfc_libre_scan.libre.Libre;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink_sas_db.RawScanTable;
import com.example.nfc_libre_scan.librelink_sas_db.SensorTable;

import java.io.IOException;

public class LibreLink implements OnLibreMessageListener, View.OnClickListener {

    private final Logger logger;
    private LibreMessage libreMessage;
    private final Activity activity;
    private final RootLib rootLib;
    private final String ourDbPath;

    LibreLink(Activity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
        this.rootLib = new RootLib(activity, logger);
        this.ourDbPath = activity.getDatabasePath("sas.db").getAbsolutePath();
    }

    public void listenLibreMessages(Libre libre) {
        libre.setLibreListener(this);
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
            removeLibreLinkDatabases();
        }
    }

    public void addLastScanToDatabase() {
        if (libreMessage == null) {
            logger.error("App does not have any libre scans");
            return;
        }

        killLibreLink();
        try {
            rootLib.copyFile(Consts.librelink_sas_db_path, ourDbPath);
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
            rootLib.copyFile(ourDbPath, Consts.librelink_sas_db_path);
            logger.ok("edited db to librelink app copied!");
        } catch (IOException e) {
            logger.error("Failed to copy db to librelink app");
            return;
        }

        try {
            rootLib.setFilePermission(Consts.librelink_sas_db_path, 660);
            logger.ok("permission 660 set to db in librelink app");
        } catch (IOException e) {
            logger.error("Failed to set 660 permission to db in librelink app");
            return;
        }
        startLibreLink();
    }

    public void removeLibreLinkDatabases() {
        killLibreLink();
        try {
            rootLib.removeFile(Consts.librelink_sas_db_path);
            rootLib.removeFile(Consts.librelink_apollo_db_path);
            logger.ok("LibreLink dbs removed");
        } catch (IOException e) {
            logger.error("Failed to remove librelink dbs");
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
        byte[] payload = libreMessage.getPayload().getValue();

        SQLiteDatabase db = SQLiteDatabase.openDatabase(activity.getDatabasePath("sas.db").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        logger.ok("database opened. Trying to write...");

        RawScanTable rawScanTable = new RawScanTable(db);
        SensorTable sensorTable = new SensorTable(db);

        rawScanTable.addNewRecord(patchInfo, payload);
        logger.ok("RawScan record done.");

        db.close();
        logger.ok("database closed");
    }

    @Override
    public void onLibreMessageReceived(LibreMessage libreMessage) {
        logger.ok("New LibreMessage received.");
        this.libreMessage = libreMessage;
    }
}
