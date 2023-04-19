package com.example.nfc_libre_scan;

import android.app.ActivityManager;
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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.nfc_libre_scan.libre.LibreMessage;

import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class WebService extends Service {
    private static boolean webServiceIsRunning = false;

    public static boolean startService(Context context) {
        if (!WebService.webServiceIsRunning) {
            Intent intent = new Intent(context, WebService.class);
            context.startForegroundService(intent);
            return true;
        }
        return false;
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

        server = new WebServer(null, WebServer.port);
        try {
            server.start();
            Toast.makeText(this, "Web server started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start web server", Toast.LENGTH_SHORT).show();
        }

        libreLink.listenLibreMessages(server);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (server != null) {
            server.stop();
        }
        Toast.makeText(this, "Web server stopped", Toast.LENGTH_SHORT).show();
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
        private final NotificationCompat.Builder builder;
        private final NotificationManager manager;
        private final int port;

        NetworkCallback(NotificationManager manager, NotificationCompat.Builder builder, ConnectivityManager connectivityManager, int port) {
            this.connectivityManager = connectivityManager;
            this.builder = builder;
            this.manager = manager;
            this.port = port;
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
                    "Another networks:\n%s\n", port, getIPs());
            builder.setContentText(text);
            manager.notify(1, builder.build());
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
