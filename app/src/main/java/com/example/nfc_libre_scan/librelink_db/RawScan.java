package com.example.nfc_libre_scan.librelink_db;

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

public class RawScan {
    private final SQLiteDatabase db;
    private final byte[] patchInfo;
    private final byte[] payload;
    private final int scanId;
    private final int sensorId;
    private final String timeZone;
    private final long timestampLocal;
    private final long timestampUTC;
    private final long computedCRC;

    private int lastScanIdForSearch = 0;

    public long getComputedCRC(){ return computedCRC; }

    public RawScan(SQLiteDatabase db, byte[] patchInfo, byte[] payload) throws IOException {
        this.db = db;
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.scanId = computeScanId();
        this.sensorId = getSensorId();
        this.timeZone = computeCurrentTimeZone();
        this.timestampUTC = computeTimestampUTC();
        this.timestampLocal = computeTimestampLocal();
        this.computedCRC = computeCRC32();
    }

    public RawScan(SQLiteDatabase db) throws IOException {
        // That constructor for AppTester.java only
        this.db = db;
        this.patchInfo = getPatchInfo();
        this.payload = getPayload();
        this.scanId = computeScanId();
        this.sensorId = getSensorId();
        this.timeZone = computeCurrentTimeZone();
        this.timestampUTC = computeTimestampUTC();
        this.timestampLocal = computeTimestampLocal();
        this.computedCRC = computeCRC32();
    }

    private int getLastScanIdForSearch(){
        if (lastScanIdForSearch == 0) {
            this.lastScanIdForSearch = this.getLastScanId();
        }
        return lastScanIdForSearch;
    }

    private int getLastScanId(){
        String sql = "SELECT scanId FROM rawScans ORDER BY scanId ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM rawScans)-1;";
        Cursor cursor = db.rawQuery(sql, null);
        int lastScanId = 0;
        if (cursor.moveToFirst()) {
            lastScanId = cursor.getInt(0);
        }
        cursor.close();
        return lastScanId;
    }

    private byte[] getPatchInfo(){
        // That method for AppTester.java only
        final int lastScanId = getLastScanIdForSearch();
        String sql = String.format("SELECT patchInfo FROM rawScans WHERE scanId='%s'", lastScanId);
        Cursor cursor = db.rawQuery(sql, null);
        byte[] patchInfo = null;
        if (cursor.moveToFirst()) {
            patchInfo = cursor.getBlob(0);
        }
        cursor.close();
        return patchInfo;
    }
    private byte[] getPayload(){
        // That method for AppTester.java only
        final int lastScanId = getLastScanIdForSearch();
        String sql = String.format("SELECT payload FROM rawScans WHERE scanId='%s'", lastScanId);
        Cursor cursor = db.rawQuery(sql, null);
        byte[] payload = null;
        if (cursor.moveToFirst()) {
            payload = cursor.getBlob(0);
        }
        cursor.close();
        return payload;
    }

    private int computeScanId() {
        return getLastScanId() + 1;
    }

    private int getSensorId() {
        String sql = "SELECT sensorId FROM sensors ORDER BY sensorId ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM sensors)-1;";
        Cursor cursor = db.rawQuery(sql, null);
        int sensorId = 1;
        if (cursor.moveToFirst()) {
            sensorId = cursor.getInt(0);
        }
        cursor.close();
        return sensorId;
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

    private String computeCurrentTimeZone() {
        return TimeZone.getDefault().getID();
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

    public void writeItselfInDB(){
        ContentValues values = new ContentValues();
        values.put("patchInfo", patchInfo);
        values.put("payload", payload);
        values.put("scanId", scanId);
        values.put("sensorId", sensorId);
        values.put("timeZone", timeZone);
        values.put("timestampLocal", timestampLocal);
        values.put("timestampUTC", timestampUTC);
        values.put("CRC", computedCRC);
        db.insert("rawScans", null, values);
    }
}
