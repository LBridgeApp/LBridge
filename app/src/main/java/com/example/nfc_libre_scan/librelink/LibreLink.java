package com.example.nfc_libre_scan.librelink;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.App;
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

    public LibreLink(Context context) throws Exception {
        new PermissionLib(context).validateOverlay();

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
        libreMessage.triggerOnMessageSentEvent();
        rootLib.copyFile(ourDbPath, LibreLink.librelink_sas_db_path);
        Logger.ok("edited db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_sas_db_path, 660);
        Logger.ok("permission 660 set to db in librelink app");

        ActivityManager activityManager = context.getSystemService(ActivityManager.class);
        List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
        int l = 5;

        startLibreLink();

        tasks = activityManager.getAppTasks();
        tasks.get(0).moveToFront();
        //activityManager.moveTaskToFront();
        int m = 5;

    }

    private void startLibreLink(){
        final String packageName = "com.freestylelibre.app.ru";
        final String activityName = "com.librelink.app.ui.SplashActivity";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        App.getInstance().getApplicationContext().startActivity(intent);
        Logger.ok("LibreLink started");
    }

    public void removeLibreLinkDatabases() throws Exception {
        killLibreLink();
        rootLib.removeFile(LibreLink.librelink_sas_db_path);
        rootLib.removeFile(LibreLink.librelink_apollo_db_path);
        Logger.ok("LibreLink dbs removed");
    }

    /*private void startLibreLink() {
        final String packageName = "com.freestylelibre.app.ru";
        final String activityName = "com.librelink.app.ui.SplashActivity";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().getApplicationContext().startActivity(intent);
        Logger.ok("LibreLink started");
    }*/

    /*private void startLibreLink(){
        Intent intent = new Intent(App.getInstance().getApplicationContext(), LibreLinkWrapper.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        App.getInstance().getApplicationContext().startActivity(intent);
    }*/

    private void killLibreLink() {
        final String packageName = "com.freestylelibre.app.ru";
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
        Logger.ok("LibreLink killed");
    }

    /*private void hideLibreLink(){
        // убираем перекрытие нашего приложения только что запущенным LibreLink.
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
        if(!appTasks.isEmpty()){
            ActivityManager.AppTask lastTask = appTasks.get(appTasks.size() - 1);
            lastTask.moveToFront();
        }
    }*/

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
