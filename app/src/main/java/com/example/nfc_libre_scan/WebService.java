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
import java.util.List;

public class WebService extends Service {
    private static boolean webServiceIsRunning = false;

    // TODO: реализовать подключение к сервису.
    public static void startService(Context context) {
        if (!WebService.webServiceIsRunning) {
            Intent intent = new Intent(context, WebService.class);
            context.startForegroundService(intent);
            Logger.ok("WebService started");
        } else {
            Logger.error("WebService already running.");
        }
    }
    private WebServer server;

    @Override
    public void onCreate() {
        new CriticalErrorHandler().setHandler();
        // TODO: Посмотреть, как работает запуск активити из-под сервиса.
        WebService.webServiceIsRunning = true;
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
            this.stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            server.stop();
        }
        Logger.ok("Web server stopped.");
        WebService.webServiceIsRunning = false;
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
