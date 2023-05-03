package com.example.nfc_libre_scan;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.libre.RawLibreData;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

class WebServer extends NanoHTTPD implements LibreMessageProvider {
    private final Context context;
    private LibreMessageListener listener;
    private final int serverPort;
    private final ConnectivityManager connectivityManager;

    private void showNotification() {
        String text = String.format("Server is working:\n" +
                "Port: %s\n" +
                "Phone network: localhost\n" +
                "Another networks:\n%s\n", serverPort, getIPs());
        Notification.HTTP_SERVER.showOrUpdate(text);
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

    public WebServer(Context context, String hostname, int port) {
        super(hostname, port);
        this.serverPort = port;
        this.context = context;
        this.connectivityManager = context.getSystemService(ConnectivityManager.class);
        this.showNotification();
        this.setNetworkListener();
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() == Method.GET) {
            return onGet(session);
        }
        if (session.getMethod() == Method.POST) {
            return onPOST(session);
        }
        return this.NOT_FOUND();
    }

    private Response onGet(IHTTPSession session) {
        return NOT_FOUND();
    }

    private Response onPOST(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        // имена заголовков здесь почему-то с маленькой буквы
        String sender = headers.get("Sender".toLowerCase());
        String messageTo = headers.get("MessageTo".toLowerCase());
        boolean senderIsXdrip = sender != null && sender.equals("Xdrip+");
        boolean messageToUs = messageTo != null && messageTo.equals("LBridge");
        if (senderIsXdrip && messageToUs) {
            try {
                final HashMap<String, String> map = new HashMap<>();
                session.parseBody(map);
                final String json = map.get("postData");

                final RawLibreData rawLibreData = new Gson().fromJson(json, RawLibreData.class);

                final LibreMessage libreMessage = LibreMessage.getInstance(context, rawLibreData);

                Logger.ok("LibreMessage received from server.");

                listener.libreMessageReceived(libreMessage);
                return this.OK();
            } catch (Exception e) {
                Logger.error(e);
                return this.INTERNAL_SERVER_ERROR();
            }
        }
        return this.NOT_FOUND();
    }

    private Response OK() {
        String msg = "200 OK";
        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, msg);
    }

    private Response INTERNAL_SERVER_ERROR() {
        String msg = "500 Internal Server Error";
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, msg);
    }

    private Response NOT_FOUND() {
        String msg = "404 NOT FOUND";
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, msg);
    }

    @Override
    public void setLibreMessageListener(LibreMessageListener listener) {
        this.listener = listener;
    }

    private void setNetworkListener() {
        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .build();
        connectivityManager.registerNetworkCallback(request, new NetworkCallback(this));
    }

    static class NetworkCallback extends ConnectivityManager.NetworkCallback {
        private final WebServer caller;

        NetworkCallback(WebServer caller) {
            this.caller = caller;
        }

        public void onAvailable(Network network) {
            caller.showNotification();
        }

        public void onLosing(Network network, int maxMsToLive) {
            caller.showNotification();
        }

        public void onLost(Network network) {
            caller.showNotification();
        }

        public void onUnavailable() {
            caller.showNotification();
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            caller.showNotification();
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            caller.showNotification();
        }

        public void onBlockedStatusChanged(Network network, boolean blocked) {
            caller.showNotification();
        }
    }
}
