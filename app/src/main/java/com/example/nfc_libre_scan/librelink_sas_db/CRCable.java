package com.example.nfc_libre_scan.librelink_sas_db;

import java.io.IOException;

public interface CRCable {
    void fillClassRelatedToLastFieldValueRecord();
    String getTableName();
    long computeCRC32() throws IOException;
    long getOriginalCRC();
}
