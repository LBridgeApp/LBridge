package com.example.nfc_libre_scan;

import com.example.nfc_libre_scan.libre.LibreMessage;

public interface OnLibreMessageListener {
    void onLibreMessageReceived(LibreMessage message);
}
