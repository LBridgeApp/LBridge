package com.example.nfc_libre_scan;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import com.example.nfc_libre_scan.libre.Libre;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink_sas_db.HistoricReadingTable;
import com.example.nfc_libre_scan.librelink_sas_db.RawScanTable;
import com.example.nfc_libre_scan.librelink_sas_db.RealTimeReadingTable;
import com.example.nfc_libre_scan.librelink_sas_db.SensorTable;

import java.io.IOException;
import java.util.Objects;

public class LibreLink implements OnLibreMessageListener, View.OnClickListener {
    private LibreMessage libreMessage;
    private final Context context;
    private final RootLib rootLib;
    private final String ourDbPath;

    LibreLink(Context context) {
        this.context = context;
        this.rootLib = new RootLib(context);
        this.ourDbPath = context.getDatabasePath("sas.db").getAbsolutePath();
    }

    public void listenLibreMessages(Libre libre) {
        libre.setLibreListener(this);
    }

    public void listenLibreMessages(WebServer server){
        server.setLibreMessageListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sugarAddingBtn || v.getId() == R.id.removeLibrelinkDB) {
            boolean granted = rootLib.requestRoot();
            if (!granted) {
                Logger.error("Root is not granted.");
                return;
            } else {
                Logger.ok("Root granted");
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
            Logger.error("App does not have any libre scans");
            return;
        }

        if (libreMessage.getLockingStatus() != LibreMessage.MessageLockingStatus.UNLOCKED) {
            Logger.error(String.format("That libre message locked.\nReason: %s", libreMessage.getLockingStatus()));
            return;
        }

        killLibreLink();
        try {
            rootLib.copyFile(Consts.librelink_sas_db_path, ourDbPath);
            Logger.ok("db to our app copied");
        } catch (IOException e) {
            Logger.error("Failed to copy db to our app");
            Logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
            return;
        }

        try {
            rootLib.setFilePermission(ourDbPath, 666);
            Logger.ok("Permission 666 set to db in our app");
        } catch (IOException e) {
            Logger.error("Failed to set 666 permission to db in our app");
            Logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
            return;
        }

        try {
            editDatabase();
            Logger.ok("database edited.");
            libreMessage.lockForSending(LibreMessage.MessageLockingStatus.MESSAGE_ALREADY_SENT);
        } catch (Exception e) {
            Logger.error("Failed to edit db in our app");
            Logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
            return;
        }

        try {
            rootLib.copyFile(ourDbPath, Consts.librelink_sas_db_path);
            Logger.ok("edited db to librelink app copied!");
        } catch (IOException e) {
            Logger.error("Failed to copy db to librelink app");
            Logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
            return;
        }

        try {
            rootLib.setFilePermission(Consts.librelink_sas_db_path, 660);
            Logger.ok("permission 660 set to db in librelink app");
        } catch (IOException e) {
            Logger.error("Failed to set 660 permission to db in librelink app");
            Logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
            return;
        }
        startLibreLink();
    }

    public void removeLibreLinkDatabases() {
        killLibreLink();
        try {
            rootLib.removeFile(Consts.librelink_sas_db_path);
            rootLib.removeFile(Consts.librelink_apollo_db_path);
            Logger.ok("LibreLink dbs removed");
        } catch (IOException e) {
            Logger.error("Failed to remove librelink dbs");
        }
    }

    private void startLibreLink() {
        final String packageName = "com.freestylelibre.app.ru";
        final String activityName = "com.librelink.app.ui.SplashActivity";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        context.startActivity(intent);
        Logger.ok("LibreLink started");
    }

    private void killLibreLink() {
        final String packageName = "com.freestylelibre.app.ru";
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
        Logger.ok("LibreLink killed");
    }

    private void editDatabase() throws Exception {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath("sas.db").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        Logger.ok("database opened. Trying to write...");

        RawScanTable rawScanTable;
        SensorTable sensorTable;
        RealTimeReadingTable realTimeReadingTable;
        HistoricReadingTable historicReadingTable;
        try {
            rawScanTable = new RawScanTable(db, libreMessage);
            sensorTable = new SensorTable(db, libreMessage);
            realTimeReadingTable = new RealTimeReadingTable(db, libreMessage);
            historicReadingTable = new HistoricReadingTable(db, libreMessage);
        } catch (Exception ignored) {
            throw new Exception("Scan the sensor in LibreLink until glucose appears.");
        }

        if (sensorTable.isSensorExpired()) {
            throw new Exception("Sensor has expired. You need to:\n" +
                    "1) Install a new one or restart old;\n" +
                    "2) Clear the LibreLink database;\n" +
                    "3) Scan the sensor in LibreLink until glucose appears.");
        }

        db.beginTransaction();

        sensorTable.updateToLastScan();
        rawScanTable.addNewSensorScan();
        realTimeReadingTable.addLastSensorScan();
        historicReadingTable.addLastSensorScan();

        db.setTransactionSuccessful();
        db.endTransaction();

        Logger.ok("Records done.");

        db.close();
        Logger.ok("database closed");
    }

    @Override
    public void onLibreMessageReceived(LibreMessage libreMessage) {
        Logger.ok("New LibreMessage received.");
        this.libreMessage = libreMessage;
    }
}
