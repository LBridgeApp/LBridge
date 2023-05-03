package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import java.io.IOException;

public interface CrcTable {
    void onTableClassInit() throws Exception;
    void fillByLastRecord();
    String getTableName();
    long computeCRC32() throws IOException;
    long getOriginalCRC();
    boolean isTableNull();
    void onTableChanged() throws Exception;
}
