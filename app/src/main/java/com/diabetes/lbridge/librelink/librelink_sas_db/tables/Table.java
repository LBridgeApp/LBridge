package com.diabetes.lbridge.librelink.librelink_sas_db.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.diabetes.lbridge.librelink.librelink_sas_db.LibreLinkDatabase;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.Row;

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
