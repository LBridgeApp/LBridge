package com.diabetes.lbridge;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import com.diabetes.lbridge.librelink.LibreLink;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class WebService extends Service {
    private WebServer server;
    private static WebService instance = null;
    private AppDatabase appDatabase;
    private LibreLink libreLink;
    private final int MIN_PORT = 1024;
    private final int MAX_PORT = 65535;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startService() {
        Logger.inf("Trying to start service...");
        Context context = App.getInstance().getApplicationContext();
        if (WebService.instance == null) {
            Intent intent = new Intent(context, WebService.class);
            context.startForegroundService(intent);
            Logger.ok("web-service started");
        } else {
            Logger.warn("web-service already running.");
        }
    }

    public static void stopService(){
        if(instance != null){
            instance.stopSelf();
        }
        else {
            Logger.warn("web-service already is not exists.");
        }
    }

    @Override
    public void onCreate() {
        WebService.instance = this;
        this.appDatabase = App.getInstance().getAppDatabase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            int serverPort = getSavedOrFindFreePort();
            this.saveGeneratedPort(serverPort);

            server = new WebServer(this, null, serverPort);
            server.start();
            libreLink = new LibreLink(this);
            libreLink.listenLibreMessages(server);

        } catch (Exception e) {
            Logger.error(e);
            String errorMsg = String.format("web-service stopped!\n" +
                    "See logs. Error msg:\n" +
                    "%s", e.getMessage());
            Notification.SERVICE_STOPPED.showOrUpdate(errorMsg);
            this.stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            server.stop();
        }

        if(libreLink != null){
            libreLink.close();
        }

        WebService.instance = null;
        Logger.inf("web-service stopped.");
        super.onDestroy();
    }

    private int getSavedOrFindFreePort() throws Exception {
        int port = readSavedPort();
        if(port == -1){
            port = generateRandomPort();
        }

        if (isPortAvailable(port)) {
            return port;
        }

        for (int p = this.MIN_PORT; p <= this.MAX_PORT; p++) {
            if (isPortAvailable(p)) {
                return p;
            }
        }
        throw new Exception("Cannot find free ports.");
    }

    private int generateRandomPort(){
        return this.MIN_PORT + new SecureRandom().nextInt(this.MAX_PORT - this.MIN_PORT + 1);
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(port));
            return true;
        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    private int readSavedPort() {
        Cursor cursor = appDatabase.getSQLite().rawQuery("SELECT value FROM options WHERE name='serverPort'", null);
        boolean recordExists = cursor.moveToPosition(0); // перемещаем курсор на строку с индексом 0;

        if(!recordExists){
            cursor.close();
            Logger.warn("Server port record does not exists.");
            return -1;
        }

        int port = cursor.getInt(0); // порт находится в колонке с индексом 0
        cursor.close();

        if(port < this.MIN_PORT || port > this.MAX_PORT){
            Logger.warn(String.format("Port %s is not valid.", port));
            return -1;
        }
        return port;
    }
    private void saveGeneratedPort(int value) throws Exception {
        appDatabase.execInTransaction(() -> {
            appDatabase.getSQLite().execSQL("INSERT OR REPLACE INTO options (name, value) VALUES (?, ?)",
                    new Object[]{ "serverPort", value });
        });
    }
}
