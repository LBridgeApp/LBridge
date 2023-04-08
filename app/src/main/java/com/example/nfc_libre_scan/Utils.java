package com.example.nfc_libre_scan;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Utils {
    public static byte[] convertByteStringToByteArray(String byteString) {
        if (byteString == null) {return null;}
        String[] hex = byteString.split("\u0020");

        byte[] byteArray = new byte[hex.length];

        for (int i = 0; i < hex.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return byteArray;
    }

    public int countStringsInTable(SQLiteDatabase db, String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s;", tableName);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}
