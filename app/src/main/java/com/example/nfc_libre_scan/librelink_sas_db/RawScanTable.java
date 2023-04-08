package com.example.nfc_libre_scan.librelink_sas_db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.zip.CRC32;

public class RawScanTable {
    private final OnNewRecordListener listener;
    private final SQLiteDatabase db;
    private byte[] patchInfo;
    private byte[] payload;
    private int scanId;
    private int sensorId;
    private String timeZone;
    private long timestampLocal;
    private long timestampUTC;
    private Integer lastScanIdForSearch;

    public RawScanTable(SQLiteDatabase db) {
        this.db = db;
        this.listener = new SqliteSequence(db);
    }
    private Object getRelatedValueForLastScanId(String fieldName) {
        final int lastScanIdForSearch = getLastScanIdForSearch();
        String sql = String.format("SELECT %s FROM %s WHERE %s='%s'", fieldName, TableStrings.TABLE_NAME, TableStrings.scanId, lastScanIdForSearch);
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

    private int getLastScanIdForSearch() {
        if(lastScanIdForSearch == null){
            String sql = "SELECT scanId FROM rawScans ORDER BY scanId ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM rawScans)-1;";
            Cursor cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                lastScanIdForSearch = cursor.getInt(0);
            }
            cursor.close();
        }

        return lastScanIdForSearch;
    }

    private int computeScanId() {
        return getLastScanIdForSearch() + 1;
    }

    private int computeSensorId() {
        String sql = "SELECT sensorId FROM sensors ORDER BY sensorId ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM sensors)-1;";
        Cursor cursor = db.rawQuery(sql, null);
        int sensorId = 1;
        if (cursor.moveToFirst()) {
            sensorId = cursor.getInt(0);
        }
        cursor.close();
        return sensorId;
    }

    private String computeCurrentTimeZone() {
        return TimeZone.getDefault().getID();
    }

    private long computeTimestampLocal() {
        // Метод toEpochMilli не учитывает временную зону.
        // Класс LocalDateTime не знает, какая у него временная зона.
        // хитрость в том, что нужно локальное время записать как UTC.
        // тогда именно локальное время будет записано в миллисекунды Unix, а не UTC!.
        Instant utc = Instant.ofEpochMilli(this.timestampUTC);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(utc, ZoneId.systemDefault());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        return zonedDateTime.toInstant().toEpochMilli();
    }

    private long computeTimestampUTC() {
        return Instant.now().toEpochMilli();
    }
    private long computeCRC32() throws IOException {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.sensorId);
        dataOutputStream.writeLong(this.timestampUTC);
        dataOutputStream.writeLong(this.timestampLocal);
        dataOutputStream.writeUTF(this.timeZone);
        dataOutputStream.write(this.patchInfo);
        dataOutputStream.write(this.payload);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    public long getComputedCRC() throws IOException {
        return computeCRC32();
    }

    public void addNewRecord(byte[] patchInfo, byte[] payload) throws IOException {
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.scanId = computeScanId();
        this.sensorId = computeSensorId();
        this.timeZone = computeCurrentTimeZone();
        this.timestampUTC = computeTimestampUTC();
        this.timestampLocal = computeTimestampLocal();
        long computedCRC = computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.patchInfo, patchInfo);
        values.put(TableStrings.payload, payload);
        values.put(TableStrings.scanId, scanId);
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.timeZone, timeZone);
        values.put(TableStrings.timestampLocal, timestampLocal);
        values.put(TableStrings.timestampUTC, timestampUTC);
        values.put(TableStrings.CRC, computedCRC);
        db.insert(TableStrings.TABLE_NAME, null, values);

        listener.onNewRecord(TableStrings.TABLE_NAME);
    }

    private static class TableStrings {
        static final String TABLE_NAME = "rawScans";
        static final String patchInfo = "patchInfo";
        static final String payload = "payload";
        static final String scanId = "scanId";
        static final String sensorId = "sensorId";
        static final String timeZone = "timeZone";
        static final String timestampUTC = "timestampUTC";
        static final String timestampLocal = "timestampLocal";
        static final String CRC = "CRC";
    }
    // ****************************CODE BELOW FOR TESTING PROPOSALS ONLY*****************************

    public void fillClassByValuesInLastRawScanRecord() {
        // That constructor for AppTester.java only
        this.patchInfo = getPatchInfo();
        this.payload = getPayload();
        this.scanId = getScanId();
        this.sensorId = getSensorId();
        this.timeZone = getTimeZone();
        this.timestampUTC = getTimestampUTC();
        this.timestampLocal = getTimestampLocal();
    }

    private byte[] getPatchInfo() {
        // That method for AppTester.java only
        return (byte[]) this.getRelatedValueForLastScanId(TableStrings.patchInfo);
    }

    private byte[] getPayload() {
        // That method for AppTester.java only
        return (byte[]) this.getRelatedValueForLastScanId(TableStrings.payload);
    }

    private int getScanId() {
        // That method for AppTester.java only
        Long l = (Long) this.getRelatedValueForLastScanId(TableStrings.scanId);
        return l.intValue();
    }

    private int getSensorId() {
        // That method for AppTester.java only
        Long l = (Long) this.getRelatedValueForLastScanId(TableStrings.sensorId);
        return l.intValue();
    }

    private String getTimeZone() {
        // That method for AppTester.java only
        return (String) this.getRelatedValueForLastScanId(TableStrings.timeZone);
    }

    private long getTimestampUTC() {
        // That method for AppTester.java only
        return (long) this.getRelatedValueForLastScanId(TableStrings.timestampUTC);
    }

    private long getTimestampLocal() {
        // That method for AppTester.java only
        return (long) this.getRelatedValueForLastScanId(TableStrings.timestampLocal);
    }
}
