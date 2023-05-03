package com.example.nfc_libre_scan;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.example.nfc_libre_scan.librelink.LibreLink;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class WebService extends Service {
    private WebServer server;
    private static WebService instance = null;
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(Notification.HTTP_SERVER.getId(), Notification.HTTP_SERVER.getBuilder().build());
        try {
            int serverPort = getSavedOrFindFreePort(1024, 65535);
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
        WebService.instance = null;
        super.onDestroy();
    }

    private Integer getSavedOrFindFreePort(int minPort, int maxPort) throws Exception {
        int savedPort = readSavedPort();
        savedPort = (savedPort >= minPort && savedPort <= maxPort) ? savedPort : minPort;
        if (isPortAvailable(savedPort)) {
            return savedPort;
        }

        for (int p = minPort; p <= maxPort; p++) {
            if (isPortAvailable(p)) {
                this.saveGeneratedPort(p);
                return p;
            }
        }
        throw new Exception("Could not find free ports.");
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
        SharedPreferences preferences = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        return preferences.getInt("ServerPort", -1);
    }

    private void saveGeneratedPort(int value) {
        SharedPreferences preferences = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("ServerPort", value);
        editor.apply();
    }
}
