package com.diabetes.lbridge.librelink.rows;

import com.diabetes.lbridge.librelink.tables.Table;


public interface Row {
    void insertOrThrow() throws Exception;

    static String getBaseUpdatingSQL(Table table, String fieldName, String tableIdField, int tableIdFieldValue){
        return String.format("UPDATE %s SET %s=? WHERE %s=%s",
                table.getName(), fieldName, tableIdField, tableIdFieldValue);
    }

    static String getBaseRowSearchingSQL(Table table){
        return String.format("SELECT * FROM %s", table.getName());
    }
}
