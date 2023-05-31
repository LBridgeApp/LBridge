package com.diabetes.lbridge.librelink;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqlUtils {
    protected static int countTables(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table'", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    protected static int countIndexes(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='index'", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
