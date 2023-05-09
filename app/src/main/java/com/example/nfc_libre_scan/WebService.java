package com.example.nfc_libre_scan;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import com.example.nfc_libre_scan.librelink.LibreLink;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WebService extends Service {
    private WebServer server;
    private final List<BroadcastReceiver> receivers = new ArrayList<>();
    private static WebService instance = null;
    private AppDatabase appDatabase;

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
            Logger.ok("WebService started");
        } else {
            Logger.warn("WebService already running.");
        }
    }

    public void addBroadcastReceiver(BroadcastReceiver receiver){
        this.receivers.add(receiver);
    }

    public static void stopService(){
        if(instance != null){
            instance.stopSelf();
            Logger.ok("WebService stopped.");
        }
        else {
            Logger.warn("WebService already is not exists.");
        }
    }

    @Override
    public void onCreate() {
        WebService.instance = this;
        this.appDatabase = App.getInstance().getAppDatabase();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(Notification.HTTP_SERVER.getId(), Notification.HTTP_SERVER.getBuilder().build());
        try {
            int serverPort = getSavedOrFindFreePort();
            this.saveGeneratedPort(serverPort);

            server = new WebServer(this, null, serverPort);
            server.start();
            Logger.ok("Web server started.");
            LibreLink libreLink = new LibreLink(this);
            libreLink.listenLibreMessages(server);

        } catch (Exception e) {
            Logger.error(e);
            String errorMsg = String.format("IMPORTANT: SERVICE STOPPED!\n" +
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
        Notification.HTTP_SERVER.cancel();
        Logger.inf("Web server stopped.");
        receivers.forEach(this::unregisterReceiver);
        receivers.clear();
        WebService.instance = null;
        super.onDestroy();
    }

    private int getSavedOrFindFreePort() throws Exception {
        int port;
        try {
            port = readSavedPort();
        }
        catch (Exception e){
            Logger.error(e);
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

    private int readSavedPort() throws Exception {
        Cursor cursor = appDatabase.getSQLite().rawQuery("SELECT name FROM options WHERE name='serverPort'", null);
        boolean recordExists = cursor.moveToPosition(0); // перемещаем курсор на строку с индексом 0;

        if(!recordExists){
            cursor.close();
            throw new Exception("No server port record found.");
        }

        int port = cursor.getInt(0); // порт находится в колонке с индексом 0
        cursor.close();

        if(port < this.MIN_PORT || port > this.MAX_PORT){
            throw new Exception(String.format("Port %s is not valid.", port));
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
