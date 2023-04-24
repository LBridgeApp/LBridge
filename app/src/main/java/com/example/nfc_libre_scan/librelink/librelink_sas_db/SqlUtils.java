package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqlUtils {
    enum Mode{ READING, WRITING }

    protected static Integer getLastStoredFieldValue(SQLiteDatabase db, String fieldName, String tableName) {
        String sql = String.format("SELECT %s FROM %s ORDER BY %s ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM %s)-1;", fieldName, tableName, fieldName, tableName);
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }
        cursor.close();
        return null;
    }

    protected static Object getRelatedValue(SQLiteDatabase db, String fieldName, String tableName, String whereFieldName, Integer whereCellValue) {
        String sql = String.format("SELECT %s FROM %s WHERE %s=%s;", fieldName, tableName, whereFieldName, whereCellValue);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        Object data = null;
        if(cursor.moveToFirst()){
            if (cursor.getType(0) == Cursor.FIELD_TYPE_INTEGER) {
                data = cursor.getLong(0);
            } else if (cursor.getType(0) == Cursor.FIELD_TYPE_FLOAT) {
                data = cursor.getFloat(0);
            } else if (cursor.getType(0) == Cursor.FIELD_TYPE_STRING) {
                data = cursor.getString(0);
            } else if (cursor.getType(0) == Cursor.FIELD_TYPE_BLOB) {
                data = cursor.getBlob(0);
            }
        }
        cursor.close();
        return data;
    }

    protected static void validateCrcAlgorithm(CrcTable table, SqlUtils.Mode mode) throws Exception {
        if(!table.isTableNull()){
            table.fillByLastRecord();
            long computedCRC = table.computeCRC32();
            long originalCRC = table.getOriginalCRC();
            if(computedCRC != originalCRC){
                throw new Exception(String.format("%s table %s test is not passed.", table.getTableName(), mode));
            }
        }
    }

    protected static boolean isTableNull(SQLiteDatabase db, String tableName){
        String sql = String.format("SELECT COUNT(*) FROM %s;", tableName);
        Cursor cursor = db.rawQuery(sql, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count == 0;
    }
}
