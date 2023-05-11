package com.diabetes.lbridge.librelink;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.diabetes.lbridge.App;
import com.diabetes.lbridge.LibreLinkActivityListener;
import com.diabetes.lbridge.LibreMessageListener;
import com.diabetes.lbridge.LibreMessageProvider;
import com.diabetes.lbridge.Logger;
import com.diabetes.lbridge.PermissionLib;
import com.diabetes.lbridge.RootLib;
import com.diabetes.lbridge.WebService;
import com.diabetes.lbridge.libre.LibreMessage;
import com.diabetes.lbridge.librelink.librelink_sas_db.LibreLinkDatabase;

import java.util.ArrayList;
import java.util.List;

public class LibreLink implements LibreMessageListener {
    @SuppressLint("SdCardPath")
    private static final String librelink_sas_db_path = "/data/data/com.freestylelibre.app.ru/files/sas.db";
    @SuppressLint("SdCardPath")
    private static final String librelink_apollo_db_path = "/data/data/com.freestylelibre.app.ru/files/apollo.db";

    private static final String APOLLO_DB = "apollo.db";
    private static final String SAS_DB = "sas.db";

    private LibreMessage libreMessage;
    private final Context context;
    private final RootLib rootLib;
    private final PermissionLib permissionLib;
    private static final List<LibreLinkActivityListener> listeners = new ArrayList<>();

    public LibreLink(Context context) throws Exception {
        this.permissionLib = new PermissionLib(context);

        this.context = context;
        this.rootLib = new RootLib(context);

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

        killApp();
        if(!isDatabasesExistsInLibreLinkApp()) {
            // удаление в rootLib безопасно в плане отсутствия файлов.
            // может отсутствовать одна база данных из двух,
            // а удалить надо все.
            this.removeDatabasesInLibreLinkApp();
            this.createDatabasesInLibreLinkApp();
        }
        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.patchWithLastScan();
        Logger.ok("database edited.");
        libreMessage.onAddedToDatabase();
        this.copySasDatabaseFromUs();
        this.removeDatabasesInOurApp();
        Logger.ok("Databases removed from our app");
        this.startApp();
    }

    private void copySasDatabaseToUs() throws Exception {
        String ourSasDbPath = context.getDatabasePath(SAS_DB).getAbsolutePath();

        rootLib.copyFile(LibreLink.librelink_sas_db_path, ourSasDbPath);
        Logger.ok("sas.db to our app copied");
        rootLib.setFilePermission(ourSasDbPath, 666);
        Logger.ok("Permission 666 set to sas.db in our app");
    }

    public void copySasDatabaseFromUs() throws Exception {
        String ourSasDbPath = context.getDatabasePath(SAS_DB).getAbsolutePath();

        rootLib.copyFile(ourSasDbPath, LibreLink.librelink_sas_db_path);
        Logger.ok("sas.db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_sas_db_path, 666);
        Logger.ok("permission 666 set to sas.db in librelink app");
    }

    public void copyApolloDatabaseFromUs() throws Exception {
        String ourApolloPath = context.getDatabasePath(APOLLO_DB).getAbsolutePath();

        rootLib.copyFile(ourApolloPath, LibreLink.librelink_apollo_db_path);
        Logger.ok("apollo.db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_apollo_db_path, 666);
        Logger.ok("permission 666 set to apollo.db in librelink app");
    }

    public void removeDatabasesInOurApp() throws Exception {
        rootLib.removeFile(context.getDatabasePath(APOLLO_DB).getAbsolutePath());
        Logger.ok("apollo.db in our app removed.");
        rootLib.removeFile(context.getDatabasePath(SAS_DB).getAbsolutePath());
        Logger.ok("sas.db in our app removed.");
    }

    public void removeDatabasesInLibreLinkApp() throws Exception {
        killApp();
        rootLib.removeFile(LibreLink.librelink_sas_db_path);
        rootLib.removeFile(LibreLink.librelink_apollo_db_path);
        Logger.ok("databases in librelink app removed.");
    }

    public boolean isDatabasesExistsInLibreLinkApp() throws Exception {
        return rootLib.isFileExists(librelink_apollo_db_path)
                && rootLib.isFileExists(librelink_sas_db_path);
    }

    public void createDatabasesInLibreLinkApp() throws Exception {
        killApp();
        this.removeDatabasesInOurApp();
        LibreLinkDatabase db = new LibreLinkDatabase(context, this);
        db.createDatabasesInOurApp();
        this.copySasDatabaseFromUs();
        this.copyApolloDatabaseFromUs();
        this.removeDatabasesInOurApp();
    }

    private void startApp() {
        // Если разрешение отсутствует,
        // из сервиса не получится корректно отправить сахар.
        if(!permissionLib.canDrawOverlays()){
            Logger.warn("No draw overlay permission.");
        }

        final String packageName = "com.freestylelibre.app.ru";
        final String activityName = "com.librelink.app.ui.SplashActivity";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().getApplicationContext().startActivity(intent);

        LibreLink.listeners.forEach(l -> l.librelinkAppeared(context instanceof Activity));
        Logger.ok("LibreLink started");
    }

    private void killApp() {
        final String packageName = "com.freestylelibre.app.ru";
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(packageName);
        Logger.ok("LibreLink killed");
    }

    @Override
    public void libreMessageReceived(LibreMessage libreMessage) {
        this.libreMessage = libreMessage;
    }

    public LibreMessage getLibreMessage(){ return libreMessage; }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        this.killApp();
        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.setFakeSerialNumberForLastSensor();
        Logger.ok("Fake serial number has been assigned to the last sensor");
        this.copySasDatabaseFromUs();
    }

    public void endLastSensor() throws Exception {
        this.killApp();
        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.endLastSensor();
        Logger.ok("Last sensor has ended in librelink db.");
        this.copySasDatabaseFromUs();
    }
}
