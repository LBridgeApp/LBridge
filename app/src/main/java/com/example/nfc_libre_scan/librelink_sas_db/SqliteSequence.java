package com.example.nfc_libre_scan.librelink_sas_db;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

public class SqliteSequence {

    private final SQLiteDatabase db;

    public SqliteSequence(SQLiteDatabase db) {
        this.db = db;
    }

    public void onNewRecordMade(String tableName) {
        int newValue = countStringsInTable(tableName);
        updateValueInSqliteSequenceTable(tableName, newValue);
    }

    private void updateValueInSqliteSequenceTable(String tableName, int newValue) {
        String sql = String.format("UPDATE sqlite_sequence SET seq=%s WHERE name=%s;", newValue, DatabaseUtils.sqlEscapeString(tableName));
        db.execSQL(sql);
    }

    private int countStringsInTable(String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s;", tableName);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
