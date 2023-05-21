package com.diabetes.lbridge.librelink;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.diabetes.lbridge.App;
import com.diabetes.lbridge.LibreLinkActivityListener;
import com.diabetes.lbridge.LibreMessageListener;
import com.diabetes.lbridge.LibreMessageProvider;
import com.diabetes.lbridge.Logger;
import com.diabetes.lbridge.PermissionLib;
import com.diabetes.lbridge.RootLib;
import com.diabetes.lbridge.WebService;
import com.diabetes.lbridge.libre.LibreMessage;

import java.util.ArrayList;
import java.util.List;

public class LibreLink implements LibreMessageListener {
    private static final String PACKAGE_NAME = "com.freestylelibre.app.ru";
    private static final String APOLLO_DB = "apollo.db";
    private static final String SAS_DB = "sas.db";
    @SuppressLint("SdCardPath")
    private static final String librelink_work_dir = String.format("/data/data/%s", PACKAGE_NAME);
    @SuppressLint("SdCardPath")
    private static final String librelink_files_dir = String.format("%s/files", librelink_work_dir);

    private static final String librelink_shared_prefs_dir = String.format("%s/shared_prefs", librelink_work_dir);
    @SuppressLint("SdCardPath")
    private static final String librelink_apollo_db_path = String.format("%s/%s", librelink_files_dir, APOLLO_DB);
    @SuppressLint("SdCardPath")
    private static final String librelink_sas_db_path = String.format("%s/%s", librelink_files_dir, SAS_DB);
    private final String our_app_apollo_db_path;
    private final String our_app_sas_db_path;
    private LibreMessage libreMessage;
    private final Context context;
    private final RootLib rootLib;
    private final PermissionLib permissionLib;
    private final PeriodicTimer periodicTimer;
    private boolean isClosed;
    private static final List<LibreLinkActivityListener> listeners = new ArrayList<>();

    public LibreLink(Context context) throws Exception {
        this.permissionLib = new PermissionLib(context);

        this.context = context;

        this.our_app_apollo_db_path = context.getDatabasePath(APOLLO_DB).getAbsolutePath();
        this.our_app_sas_db_path = context.getDatabasePath(SAS_DB).getAbsolutePath();

        this.rootLib = new RootLib(context);
        this.periodicTimer = (context instanceof WebService) ? new PeriodicTimer(this, (WebService) context) : null;
        if (context instanceof WebService) {
            periodicTimer.start();
        }
    }

    public static void setLibreLinkActivityListener(LibreLinkActivityListener listener) {
        LibreLink.listeners.add(listener);
    }

    public void listenLibreMessages(LibreMessageProvider provider) throws Exception {
        this.requireOpen();

        provider.setLibreMessageListener(this);
    }

    public void addLastScanToDatabase() throws Exception {
        this.requireOpen();

        validateInstallation();
        validateAuthorization();

        if (libreMessage == null) {
            throw new Exception("LibreMessage is null");
        }

        if (libreMessage.getLockingStatus() != LibreMessage.MessageLockingStatus.UNLOCKED) {
            throw new Exception(String.format("That libre message locked.\nReason: %s", libreMessage.getLockingStatus()));
        }

        if (!isApolloExistsInLibreLinkApp() || !isSasExistsInLibreLinkApp()) {
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

    public void removeDatabasesInOurApp() throws Exception {
        this.requireOpen();

        rootLib.removeFile(this.our_app_apollo_db_path);
        Logger.ok("apollo.db in our app removed.");
        rootLib.removeFile(this.our_app_sas_db_path);
        Logger.ok("sas.db in our app removed.");
    }

    public void createDatabasesInLibreLinkApp() throws Exception {
        this.requireOpen();

        this.removeDatabasesInOurApp();
        LibreLinkDatabase db = new LibreLinkDatabase(context, this);
        db.createDatabasesInOurApp();
        this.copySasDatabaseFromUs();
        this.copyApolloDatabaseFromUs();
        this.removeDatabasesInOurApp();
    }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        this.requireOpen();

        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.setFakeSerialNumberForLastSensor();
        this.copySasDatabaseFromUs();
        Logger.ok("Fake serial number has been assigned to the last sensor");
    }

    public void endLastSensor() throws Exception {
        this.requireOpen();

        this.copySasDatabaseToUs();
        LibreLinkDatabase libreLinkDatabase = new LibreLinkDatabase(this.context, this);
        libreLinkDatabase.endLastSensor();
        this.copySasDatabaseFromUs();
        Logger.ok("Last sensor has ended in librelink db.");
    }

    protected LibreMessage getLibreMessage() {
        return libreMessage;
    }

    public void close() {
        rootLib.close();
        if (periodicTimer != null) {
            periodicTimer.stop();
        }
        isClosed = true;
        Logger.inf("LibreLink closed.");
    }

    private void copySasDatabaseToUs() throws Exception {
        rootLib.copyFile(LibreLink.librelink_sas_db_path, this.our_app_sas_db_path);
        Logger.ok("sas.db to our app copied");
        rootLib.setFilePermission(this.our_app_sas_db_path, 666);
        Logger.ok("Permission 666 set to sas.db in our app");
    }

    private void copySasDatabaseFromUs() throws Exception {
        killApp();
        rootLib.copyFile(our_app_sas_db_path, LibreLink.librelink_sas_db_path);
        Logger.ok("sas.db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_sas_db_path, 666);
        Logger.ok("permission 666 set to sas.db in librelink app");
    }

    private void copyApolloDatabaseFromUs() throws Exception {
        killApp();
        rootLib.copyFile(our_app_apollo_db_path, LibreLink.librelink_apollo_db_path);
        Logger.ok("apollo.db to librelink app copied!");
        rootLib.setFilePermission(LibreLink.librelink_apollo_db_path, 666);
        Logger.ok("permission 666 set to apollo.db in librelink app");
    }

    private void validateInstallation() throws Exception {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new PackageManager.NameNotFoundException("Russian librelink version 2.8.2 is not installed.");
        }

        String installedVersion = packageInfo.versionName;

        if (!installedVersion.equals("2.8.2")) {
            throw new Exception(String.format("Installed version of Russian LibreLink: %s. " +
                    "Is the app updated from Google Play? " +
                    "You need to install 2.8.2", installedVersion));
        }

        Logger.ok("Russian LibreLink 2.8.2 is installed.");
    }

    private void validateAuthorization() throws Exception {
        Exception noAuthException = new Exception("No authorization in LibreLink");

        if (!rootLib.isFileExists(librelink_work_dir)) {
            throw noAuthException;
        }

        if (!rootLib.isFileExists(LibreLink.librelink_shared_prefs_dir)) {
            throw noAuthException;
        }

        String xmlConfigFileName = String.format("%s/%s.xml", LibreLink.librelink_shared_prefs_dir, PACKAGE_NAME);
        if (!rootLib.isFileExists(xmlConfigFileName)) {
            throw noAuthException;
        }

        String xmlString = rootLib.readTextFile(xmlConfigFileName);

        XmlConfig xmlConfig = new XmlConfig(xmlString);

        boolean authorized = !xmlConfig.getAccountlessState();
        String userFirstName = xmlConfig.getUserFirstName();
        String userLastName = xmlConfig.getUserLastName();

        if (!authorized || userFirstName == null || userLastName == null) {
            throw noAuthException;
        }

        Logger.ok(String.format("User %s %s is authorized.", userFirstName, userLastName));
    }

    private void removeDatabasesInLibreLinkApp() throws Exception {
        killApp();
        rootLib.removeFile(LibreLink.librelink_sas_db_path);
        rootLib.removeFile(LibreLink.librelink_apollo_db_path);
        Logger.ok("databases in librelink app removed.");
    }

    private boolean isApolloExistsInLibreLinkApp() throws Exception {
        return rootLib.isFileExists(librelink_apollo_db_path);
    }

    private boolean isSasExistsInLibreLinkApp() throws Exception {
        return rootLib.isFileExists(librelink_sas_db_path);
    }

    private void startApp() {
        // TODO: иногда при запуске либрелинк белый экран,
        // в логах no active focused window
        // посмотрим, решит ли CATEGORY_LAUNCHER проблему.

        // Если разрешение отсутствует,
        // из сервиса не получится корректно отправить сахар.
        if (!permissionLib.canDrawOverlays()) {
            Logger.warn("No draw overlay permission.");
        }

        final String activityName = "com.librelink.app.ui.SplashActivity";
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName(PACKAGE_NAME, activityName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        App.getInstance().getApplicationContext().startActivity(intent);

        LibreLink.listeners.forEach(l -> l.librelinkAppeared(context instanceof Activity));
        Logger.ok("LibreLink started");
    }

    private void killApp() throws Exception {
        rootLib.killApp(PACKAGE_NAME);
        Logger.ok("LibreLink killed");
    }

    @Override
    public void libreMessageReceived(LibreMessage libreMessage) {
        this.libreMessage = libreMessage;
    }

    private void requireOpen() throws Exception {
        if (isClosed) {
            throw new Exception("LibreLink closed.");
        }
    }
}
