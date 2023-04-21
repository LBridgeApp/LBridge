package com.example.nfc_libre_scan;

import android.content.Context;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.libre.RawLibreData;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

class WebServer extends NanoHTTPD {
    private final Context context;
    public static final int port = 4545;
    private OnLibreMessageListener listener;
    public WebServer(Context context, String hostname, int port) {
        super(hostname, port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        if(session.getMethod() == Method.GET){
            return onGet(session);
        }
        if(session.getMethod() == Method.POST){
            return onPOST(session);
        }
        return this.NOT_FOUND();
    }

    private Response onGet(IHTTPSession session){
        return NOT_FOUND();
    }

    private Response onPOST(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        // имена заголовков здесь почему-то с маленькой буквы
        String sender = headers.get("Sender".toLowerCase());
        String messageTo = headers.get("MessageTo".toLowerCase());
        if(sender != null && sender.equals("Xdrip+")){
            if(messageTo != null && messageTo.equals("LibreviewBridge")){
                try {
                    final HashMap<String, String> map = new HashMap<>();
                    session.parseBody(map);
                    final String json = map.get("postData");

                    RawLibreData rawLibreData = new Gson().fromJson(json, RawLibreData.class);

                    LibreMessage libreMessage = LibreMessage.getInstance(context, rawLibreData);

                    Logger.ok("LibreMessage received from server.");

                    listener.onLibreMessageReceived(libreMessage);
                    return this.OK();
                } catch (Exception e) {
                    return this.INTERNAL_SERVER_ERROR();
                }
            }
        }
        return this.NOT_FOUND();
    }

    private Response OK(){
        String msg = "200 OK";
        return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, msg);
    }

    private Response INTERNAL_SERVER_ERROR(){
        String msg = "500 Internal Server Error";
        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, msg);
    }

    private Response NOT_FOUND() {
        String msg = "404 NOT FOUND";
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, msg);
    }

    public void setLibreMessageListener(OnLibreMessageListener listener){
        this.listener = listener;
    }
}
