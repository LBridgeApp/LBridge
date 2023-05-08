package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.SensorRow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

public interface Table {
    String getName();
    Row[] queryRows();
    void rowInserted();
    LibreLinkDatabase getDatabase();

    static int getRowLength(SQLiteDatabase db, Table table){
        String query = "SELECT COUNT(*) FROM " + table.getName();
        Cursor cursor = db.rawQuery(query, null);
        int rowCount = 0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                rowCount = cursor.getInt(0);
            }
            cursor.close();
        }

        return rowCount;
    }
}
