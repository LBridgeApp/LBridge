package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import java.io.IOException;

public interface Row {
    void insertOrThrow() throws IOException;

    static String getBaseUpdatingSQL(Table table, String fieldName, String tableIdField, int tableIdFieldValue){
        return String.format("UPDATE %s SET %s=? WHERE %s=%s",
                table.getName(), fieldName, tableIdField, tableIdFieldValue);
    }

    static String getBaseRowSearchingSQL(Table table){
        return String.format("SELECT * FROM %s", table.getName());
    }
}
