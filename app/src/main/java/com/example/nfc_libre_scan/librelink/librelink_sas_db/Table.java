package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.database.Cursor;

import java.io.IOException;

public interface Table {
    String getName();
    Row[] queryRows();
    LibreLinkDatabase getDatabase();
}
