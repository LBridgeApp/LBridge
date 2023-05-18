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
import com.diabetes.lbridge.librelink.sas_db.LibreLinkDatabase;

import java.util.ArrayList;
import java.util.List;

public class LibreLink implements LibreMessageListener {
    private static final String PACKAGE_NAME = "com.freestylelibre.app.ru";
    private static final String APOLLO_DB = "apollo.db";
    private static final String SAS_DB = "sas.db";

    @SuppressLint("SdCardPath")
    private static final String librelink_apollo_db_path = String.format("/data/data/%s/files/%s", PACKAGE_NAME, APOLLO_DB);
    @SuppressLint("SdCardPath")
    private static final String librelink_sas_db_path = String.format("/data/data/%s/files/%s", PACKAGE_NAME, SAS_DB);
    private final String our_app_apollo_db_path;
    private final String our_app_sas_db_path;
    private LibreMessage libreMessage;
    private final Context context;
    private final RootLib rootLib;
    private final PermissionLib permissionLib;
    private static final List<LibreLinkActivityListener> listeners = new ArrayList<>();

    public LibreLink(Context context) throws Exception {
        this.permissionLib = new PermissionLib(context);

        this.context = context;

        this.our_app_apollo_db_path = context.getDatabasePath(APOLLO_DB).getAbsolutePath();
        this.our_app_sas_db_path = context.getDatabasePath(SAS_DB).getAbsolutePath();

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
            throw new Exception("LibreMessage is null");
        }

        if (libreMessage.getLockingStatus() != LibreMessage.MessageLockingStatus.UNLOCKED) {
            throw new Exception(String.format("That libre message locked.\nReason: %s", libreMessage.getLockingStatus()));
        }

        if(!isApolloExistsInLibreLinkApp() || !isSasExistsInLibreLinkApp()) {
            // если какой-то из файлов отсутствует,
            // то надо удалить все и создать новые.
            // удаление в rootLib безопасно в плане удаления отсутствующих файлов.
            this.removeDatabasesInLibreLinkApp();
            this.createDatabasesInLibreLinkApp();
        }
        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.patchWithLastScan();
        libreMessage.onAddedToDatabase();
        this.copySasDatabaseFromUs();
        this.removeDatabasesInOurApp();
        this.startApp();
    }

    private void copySasDatabaseToUs() throws Exception {
        rootLib.copyFile(LibreLink.librelink_sas_db_path, this.our_app_sas_db_path);
        Logger.ok("sas.db to our app copied");
        rootLib.setFilePermission(this.our_app_sas_db_path, 666);
        Logger.ok("Permission 666 set to sas.db in our app");
    }

    public void copySasDatabaseFromUs() throws Exception {
        killApp();
        rootLib.copyFile(our_app_sas_db_path, LibreLink.librelink_sas_db_path);
        Logger.ok("sas.db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_sas_db_path, 666);
        Logger.ok("permission 666 set to sas.db in librelink app");
    }

    public void copyApolloDatabaseFromUs() throws Exception {
        killApp();
        rootLib.copyFile(our_app_apollo_db_path, LibreLink.librelink_apollo_db_path);
        Logger.ok("apollo.db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_apollo_db_path, 666);
        Logger.ok("permission 666 set to apollo.db in librelink app");
    }

    public void removeDatabasesInOurApp() throws Exception {
        rootLib.removeFile(this.our_app_apollo_db_path);
        Logger.ok("apollo.db in our app removed.");
        rootLib.removeFile(this.our_app_sas_db_path);
        Logger.ok("sas.db in our app removed.");
    }

    public void removeDatabasesInLibreLinkApp() throws Exception {
        killApp();
        rootLib.removeFile(LibreLink.librelink_sas_db_path);
        rootLib.removeFile(LibreLink.librelink_apollo_db_path);
        Logger.ok("databases in librelink app removed.");
    }

    public boolean isApolloExistsInLibreLinkApp() throws Exception {
        return rootLib.isFileExists(librelink_apollo_db_path);
    }

    public boolean isSasExistsInLibreLinkApp() throws Exception {
        return rootLib.isFileExists(librelink_sas_db_path);
    }

    public void createDatabasesInLibreLinkApp() throws Exception {
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

    private void killApp() throws Exception {
        // я знаю про более мягкий способ убийства приложений
        // через ActivityManager.killBackgroundProcesses()
        // Этот способ не подошел, так как главная активити либрелинка
        // не всегда корректно убивалась, и последующий старт приложения
        // не всегда, но часто приводил к белому постоянному экрану либрелинк
        // приходилось руками убивать в настройках и запускать либрелинк в лаунчере,
        // чтобы отправить сахар в libreview.

        rootLib.killApp(PACKAGE_NAME);
        Logger.ok("LibreLink killed");
    }

    @Override
    public void libreMessageReceived(LibreMessage libreMessage) {
        this.libreMessage = libreMessage;
    }

    public LibreMessage getLibreMessage(){ return libreMessage; }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.setFakeSerialNumberForLastSensor();
        this.copySasDatabaseFromUs();
        Logger.ok("Fake serial number has been assigned to the last sensor");
    }

    public void endLastSensor() throws Exception {
        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.endLastSensor();
        this.copySasDatabaseFromUs();
        Logger.ok("Last sensor has ended in librelink db.");
    }
}
