package com.example.nfc_libre_scan.librelink_sas_db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class GeneralUtils {

    protected static Integer getLastFieldValueForSearch(SQLiteDatabase db, String fieldName, String tableName) {
        String sql = String.format("SELECT %s FROM %s ORDER BY %s ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM %s)-1;", fieldName, tableName, fieldName, tableName);
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }
        cursor.close();
        return null;
    }

    protected static Object getRelatedValue(SQLiteDatabase db, String fieldName, String tableName, String whereFieldName, int whereCellValueForSearch) {
        String sql = String.format("SELECT %s FROM %s WHERE %s='%s'", fieldName, tableName, whereFieldName, whereCellValueForSearch);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        Object data = null;
        if (cursor.getType(0) == Cursor.FIELD_TYPE_INTEGER) {
            data = cursor.getLong(0);
        } else if (cursor.getType(0) == Cursor.FIELD_TYPE_FLOAT) {
            data = cursor.getFloat(0);
        } else if (cursor.getType(0) == Cursor.FIELD_TYPE_STRING) {
            data = cursor.getString(0);
        } else if (cursor.getType(0) == Cursor.FIELD_TYPE_BLOB) {
            data = cursor.getBlob(0);
        }
        cursor.close();
        return data;
    }

    protected static int computeSensorId(SQLiteDatabase db) {
        String sql = "SELECT sensorId FROM sensors ORDER BY sensorId ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM sensors)-1;";
        Cursor cursor = db.rawQuery(sql, null);
        int sensorId = 1;
        if (cursor.moveToFirst()) {
            sensorId = cursor.getInt(0);
        }
        cursor.close();
        return sensorId;
    }

    protected static long computeTimestampLocal(long timestampUTC) {
        // Метод toEpochMilli не учитывает временную зону.
        // Класс LocalDateTime не знает, какая у него временная зона.
        // хитрость в том, что нужно локальное время записать как UTC.
        // тогда именно локальное время будет записано в миллисекунды Unix, а не UTC!.
        Instant utc = Instant.ofEpochMilli(timestampUTC);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(utc, ZoneId.systemDefault());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        return zonedDateTime.toInstant().toEpochMilli();
    }

    protected static String computeCurrentTimeZone() {
        return TimeZone.getDefault().getID();
    }

    protected static long computeTimestampUTC() {
        return Instant.now().toEpochMilli();
    }
}
