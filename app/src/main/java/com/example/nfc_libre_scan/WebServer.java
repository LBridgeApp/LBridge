package com.example.nfc_libre_scan;

import com.example.nfc_libre_scan.libre.LibreMessage;

import fi.iki.elonen.NanoHTTPD;

class WebServer extends NanoHTTPD {
    public static final int port = 4545;
    private OnLibreMessageListener listener;
    public WebServer(String hostname, int port) {
        super(hostname, port);
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
        return this.NOT_FOUND();
    }

    private Response onPOST(IHTTPSession session){
        //listener.onLibreMessageReceived(new LibreMessage(null, null, null, null, null));
        return this.NOT_FOUND();
    }

    private Response NOT_FOUND() {
        String msg = "404 NOT FOUND";
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, msg);
    }

    public void setLibreMessageListener(OnLibreMessageListener listener){
        this.listener = listener;
    }
}
