package com.example.nfc_libre_scan.librelink;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.App;
import com.example.nfc_libre_scan.LibreLinkActivityListener;
import com.example.nfc_libre_scan.LibreMessageListener;
import com.example.nfc_libre_scan.LibreMessageProvider;
import com.example.nfc_libre_scan.Logger;
import com.example.nfc_libre_scan.PermissionLib;
import com.example.nfc_libre_scan.RootLib;
import com.example.nfc_libre_scan.WebService;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.HistoricReadingTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.RawScanTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.RealTimeReadingTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.SensorTable;

import java.util.ArrayList;
import java.util.List;

public class LibreLink implements LibreMessageListener {
    @SuppressLint("SdCardPath")
    private static final String librelink_sas_db_path = "/data/data/com.freestylelibre.app.ru/files/sas.db";
    @SuppressLint("SdCardPath")
    private static final String librelink_apollo_db_path = "/data/data/com.freestylelibre.app.ru/files/apollo.db";
    private LibreMessage libreMessage;
    private final Context context;
    private final RootLib rootLib;
    private final String ourDbPath;
    private final PermissionLib permissionLib;
    private static final List<LibreLinkActivityListener> listeners = new ArrayList<>();

    public LibreLink(Context context) throws Exception {
        this.permissionLib = new PermissionLib(context);
        // проверяем разрешение при запуске.
        permissionLib.validateDrawOverlays();

        this.context = context;
        this.rootLib = new RootLib(context);
        this.ourDbPath = context.getDatabasePath("sas.db").getAbsolutePath();

        if (context instanceof WebService) {
            PeriodicTimer timer = new PeriodicTimer(this, (WebService) context);
            timer.start();
        }
    }

    public void listenLibreMessages(LibreMessageProvider provider) {
        provider.setLibreMessageListener(this);
    }

    public static void setLibreLinkActivityListener(LibreLinkActivityListener listener){
        LibreLink.listeners.add(listener);
    }

    public void addLastScanToDatabase() throws Exception {

        if (libreMessage == null) {
            throw new Exception("LibreLink object does not have any libre scans");
        }

        if (libreMessage.getLockingStatus() != LibreMessage.MessageLockingStatus.UNLOCKED) {
            throw new Exception(String.format("That libre message locked.\nReason: %s", libreMessage.getLockingStatus()));
        }

        killLibreLink();
        rootLib.copyFile(LibreLink.librelink_sas_db_path, ourDbPath);
        Logger.ok("db to our app copied");
        rootLib.setFilePermission(ourDbPath, 666);
        Logger.ok("Permission 666 set to db in our app");
        editDatabase();
        Logger.ok("database edited.");
        libreMessage.onAddedToDatabase();
        rootLib.copyFile(ourDbPath, LibreLink.librelink_sas_db_path);
        Logger.ok("edited db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_sas_db_path, 660);
        Logger.ok("permission 660 set to db in librelink app");

        startLibreLink();

    }
    private void startLibreLink() throws Exception {
        // Несмотря на проверку в конструкторе...
        // Если разрешение вдруг убрали,
        // из сервиса не получится корректно отправить сахар.
        permissionLib.validateDrawOverlays();

        final String packageName = "com.freestylelibre.app.ru";
        final String activityName = "com.librelink.app.ui.HomeActivity";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().getApplicationContext().startActivity(intent);

        LibreLink.listeners.forEach(l -> l.librelinkAppeared(context instanceof Activity));
        Logger.ok("LibreLink started");
    }

    private void killLibreLink() {
        final String packageName = "com.freestylelibre.app.ru";
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
        Logger.ok("LibreLink killed");
    }

    public void removeLibreLinkDatabases() throws Exception {
        killLibreLink();
        rootLib.removeFile(LibreLink.librelink_sas_db_path);
        rootLib.removeFile(LibreLink.librelink_apollo_db_path);
        Logger.ok("LibreLink dbs removed");
    }

    private void editDatabase() throws Exception {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath("sas.db").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        Logger.ok("database opened. Trying to write...");

        try {
            SensorTable sensorTable = new SensorTable(db, libreMessage);
            RawScanTable rawScanTable = new RawScanTable(db, sensorTable, libreMessage);
            RealTimeReadingTable realTimeReadingTable = new RealTimeReadingTable(db, sensorTable, libreMessage);
            HistoricReadingTable historicReadingTable = new HistoricReadingTable(db, sensorTable, libreMessage);

            db.beginTransaction();

            sensorTable.updateToLastScan();
            rawScanTable.addLastSensorScan();
            realTimeReadingTable.addLastSensorScan();
            historicReadingTable.addLastSensorScan();

            db.setTransactionSuccessful();

            db.endTransaction();
            db.close();
        } catch (Exception e) {
            db.endTransaction();
            db.close();
            throw e;
        }
    }

    @Override
    public void libreMessageReceived(LibreMessage libreMessage) {
        Logger.ok("New LibreMessage received.");
        this.libreMessage = libreMessage;
    }
}
