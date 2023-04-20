package com.example.nfc_libre_scan;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.List;

public class WebService extends Service {
    private static boolean webServiceIsRunning = false;

    public static void startService(Context context) {
        if (!WebService.webServiceIsRunning) {
            Intent intent = new Intent(context, WebService.class);
            context.startForegroundService(intent);
            Logger.ok("WebService started");
        }
        Logger.error("WebService already running.");
    }

    private NotificationManager notificationManager;
    private ConnectivityManager connectivityManager;
    private NotificationCompat.Builder notificationBuilder;
    private WebServer server;
    private LibreLink libreLink;

    @Override
    public void onCreate() {
        WebService.webServiceIsRunning = true;
        this.notificationManager = getSystemService(NotificationManager.class);
        this.connectivityManager = this.getSystemService(ConnectivityManager.class);
        this.notificationBuilder = getNotificationBuilder();
        this.setNetworkListener();
        libreLink = new LibreLink(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, notificationBuilder.build());

        server = new WebServer(this,null, WebServer.port);
        try {
            server.start();
            Logger.ok("Web server started.");
        } catch (Exception e) {
            Logger.error("Failed to start web server.");
            Logger.error(e.getLocalizedMessage());
        }

        libreLink.listenLibreMessages(server);
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
                .setContentText("Server IP address detection...")
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
        connectivityManager.registerNetworkCallback(request, new NetworkCallback(notificationManager, notificationBuilder, connectivityManager, WebServer.port));
    }

    static class NetworkCallback extends ConnectivityManager.NetworkCallback {
        private final ConnectivityManager connectivityManager;
        private final NotificationCompat.Builder notificationBuilder;
        private final NotificationManager notificationManager;
        private final int serverPort;

        NetworkCallback(NotificationManager notificationManager, NotificationCompat.Builder notificationBuilder, ConnectivityManager connectivityManager, int serverPort) {
            this.connectivityManager = connectivityManager;
            this.notificationBuilder = notificationBuilder;
            this.notificationManager = notificationManager;
            this.serverPort = serverPort;
        }

        public void onAvailable(Network network) {
            this.changeNotification();
        }

        public void onLosing(Network network, int maxMsToLive) {
            this.changeNotification();
        }

        public void onLost(Network network) {
            this.changeNotification();
        }

        public void onUnavailable() {
            this.changeNotification();
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            this.changeNotification();
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            this.changeNotification();
        }

        public void onBlockedStatusChanged(Network network, boolean blocked) {
            this.changeNotification();
        }

        private void changeNotification() {
            String text = String.format("Where to send libreMessages:\n" +
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
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
