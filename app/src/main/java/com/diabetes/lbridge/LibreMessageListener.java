package com.diabetes.lbridge;

import com.diabetes.lbridge.libre.LibreMessage;

public interface LibreMessageListener {
    void libreMessageReceived(LibreMessage message);
}
