package com.example.nfc_libre_scan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

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

    private NotificationManager notificationManager;
    private ConnectivityManager connectivityManager;
    private NotificationCompat.Builder notificationBuilder;
    private WebServer server;
    private LibreLink libreLink;
    private int serverPort;

    @Override
    public void onCreate() {
        Thread.setDefaultUncaughtExceptionHandler(new CriticalErrorHandler(this.getApplicationContext()));

        // TODO: Посмотреть, как работает запуск активити из-под сервиса.

        WebService.webServiceIsRunning = true;
        this.notificationManager = getSystemService(NotificationManager.class);
        this.notificationBuilder = getNotificationBuilder();
        this.connectivityManager = this.getSystemService(ConnectivityManager.class);
        this.setNetworkListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, notificationBuilder.build());
        try {
            serverPort = getSavedOrFindFreePort(1024, 65535);
            server = new WebServer(this, null, serverPort);
            server.start();
            Logger.ok("Web server started.");
            this.changeNotification();
            libreLink = new LibreLink(this);
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

    private NotificationChannel createNotificationChannel() {
        String channelId = "HTTP_SERVER";
        NotificationChannel channel;
        channel = new NotificationChannel(channelId, "HTTP_SERVER", NotificationManager.IMPORTANCE_NONE);
        notificationManager.createNotificationChannel(channel);
        return channel;
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        NotificationChannel channel = createNotificationChannel();
        String channelId = channel.getId();

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(null);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("LibreTools")
                .setContentText("Server is starting...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setStyle(bigTextStyle);

        builder.setChannelId(channelId);
        return builder;
    }

    private void setNetworkListener() {
        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build();
        connectivityManager.registerNetworkCallback(request, new NetworkCallback(this));
    }

    static class NetworkCallback extends ConnectivityManager.NetworkCallback {
        private final WebService caller;

        NetworkCallback(WebService caller) {
            this.caller = caller;
        }

        public void onAvailable(Network network) {
            caller.changeNotification();
        }

        public void onLosing(Network network, int maxMsToLive) {
            caller.changeNotification();
        }

        public void onLost(Network network) {
            caller.changeNotification();
        }

        public void onUnavailable() {
            caller.changeNotification();
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            caller.changeNotification();
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            caller.changeNotification();
        }

        public void onBlockedStatusChanged(Network network, boolean blocked) {
            caller.changeNotification();
        }
    }

    private void changeNotification() {
        String text = String.format("Server is working:\n" +
                "Port: %s\n" +
                "Phone network: localhost ( 127.0.0.1 )\n" +
                "Another networks:\n%s\n", serverPort, getIPs());
        notificationBuilder.setContentText(text);
        notificationManager.notify(1, notificationBuilder.build());
    }

    private String getIPs() {
        Network activeNetwork = connectivityManager.getActiveNetwork();
        List<LinkAddress> addresses;
        StringBuilder sb = new StringBuilder();
        if (activeNetwork != null) {
            addresses = connectivityManager.getLinkProperties(connectivityManager.getActiveNetwork()).getLinkAddresses();
            int counter = 0;
            for (LinkAddress address : addresses) {
                sb.append(String.format("#%s: %s\n", ++counter, address.getAddress().getHostAddress()));
            }
            return sb.toString();
        }
        return "Not available";
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
