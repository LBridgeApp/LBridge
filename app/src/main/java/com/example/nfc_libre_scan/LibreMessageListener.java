package com.example.nfc_libre_scan;

import com.example.nfc_libre_scan.libre.LibreMessage;

public interface LibreMessageListener {
    void libreMessageReceived(LibreMessage message);
}
