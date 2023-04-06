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
    private SQLiteDatabase db;
    private final byte[] patchInfo;
    private final byte[] payload;
    private final int scanId;
    private final int sensorId;
    private final String timeZone;
    private final long timestampLocal;
    private final long timestampUTC;
    private final long CRC;

    private RawScan(byte[] patchInfo, byte[] payload,
            int scanId, int sensorId, String timeZone,
            long timestampLocal, long timestampUTC) throws IOException {
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.scanId = scanId;
        this.sensorId = sensorId;
        this.timeZone = timeZone;
        this.timestampLocal = timestampLocal;
        this.timestampUTC = timestampUTC;
        this.CRC = getCRC32();

    }

    public RawScan(SQLiteDatabase db, byte[] patchInfo, byte[] payload) throws IOException {
        this.db = db;
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.scanId = getScanId();
        this.sensorId = getSensorId();
        this.timeZone = getCurrentTimeZone();
        this.timestampUTC = getTimestampUTC();
        this.timestampLocal = getTimestampLocal();
        this.CRC = getCRC32();
    }

    private int getScanId() {
        String sql = "SELECT scanId FROM rawScans ORDER BY scanId ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM rawScans)-1;";
        Cursor cursor = db.rawQuery(sql, null);
        int scanId = 1;
        if (cursor.moveToFirst()) {
            int lastScanId = cursor.getInt(0);
            scanId = lastScanId + 1;
        }
        cursor.close();
        return scanId;
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

    private long getTimestampLocal() {
        // Метод toEpochMilli не учитывает временную зону.
        // Класс LocalDateTime не знает, какая у него временная зона.
        // хитрость в том, что нужно локальное время записать как UTC.
        // тогда именно локальное время будет записано в миллисекунды Unix, а не UTC!.
        Instant utc = Instant.ofEpochMilli(this.timestampUTC);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(utc, ZoneId.systemDefault());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        return zonedDateTime.toInstant().toEpochMilli();
    }

    private long getTimestampUTC() {
        return Instant.now().toEpochMilli();
    }

    private String getCurrentTimeZone() {
        return TimeZone.getDefault().getID();
    }

    private long getCRC32() throws IOException {
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
        values.put("CRC", CRC);
        db.insert("rawScans", null, values);
    }

    public static boolean testCRC32(){
        final String patchInfoStr = "a2 08 00 08 55 2c";
        String[] patchInfoHexs = patchInfoStr.split("\u0020");

        byte[] patchInfo = new byte[patchInfoHexs.length];

        for (int i = 0; i < patchInfoHexs.length; i++) {
            patchInfo[i] = (byte) Integer.parseInt(patchInfoHexs[i], 16);
        }

        final String payloadStr = "9d f4 88 1b 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 7f 79 0c 0b 08 06 c8 a4 1b 80 03 06 c8 90 1b 80 01 06 c8 a0 1b 80 ff 05 c8 b4 1b 80 12 06 c8 e0 1b 80 14 06 c8 fc 5b 00 0b 06 c8 f4 5b 00 f8 05 c8 ec 5b 00 ea 05 c8 c4 5b 00 d4 05 c8 b8 1b 80 ed 05 c8 d4 1b 80 e6 05 c8 b0 5b 80 1d 06 c8 c8 5b 80 0a 06 c8 b8 5b 80 06 06 c8 b0 5b 80 0e 06 c8 98 5b 80 d6 04 c8 70 5c 00 36 05 c8 4c 1c 80 9b 05 c8 2c 1c 80 df 05 c8 d8 5b 80 11 06 c8 e4 1b 80 f1 05 c8 f0 5b 00 f5 05 c8 dc 5b 80 09 06 c8 cc 5b 80 3e 06 c8 ec 9b 80 19 06 c8 ac 1b 80 09 06 c8 e0 1b 80 df 03 c8 64 18 80 f1 03 c8 10 5a 80 81 04 c8 98 5a 80 1a 05 c8 f4 9a 80 87 05 c8 ac 1b 80 ce 05 c8 94 19 80 ff 05 c8 c8 18 80 3d 05 c8 ec 9a 80 e6 04 c8 50 5b 80 a2 04 c8 84 1b 80 84 04 c8 f8 5b 00 66 04 c8 e4 1b 80 2c 04 c8 70 1c 80 ec 03 c8 54 1c 80 c8 03 c8 34 1c 80 95 03 c8 5c 5c 00 96 03 c8 e0 5b 80 b0 03 c8 08 5c 80 f9 03 c8 58 1c 80 48 04 c8 7c 5c 00 83 04 c8 70 5c 00 0c 0a 00 00 92 31 00 08 df 0d 18 51 14 07 96 80 5a 00 ed a6 00 82 1a c8 04 d1 d7 5d";
        String[] payloadHexs = payloadStr.split("\u0020");

        byte[] payload = new byte[payloadHexs.length];
        for (int i = 0; i < payloadHexs.length; i++) {
            payload[i] = (byte) Integer.parseInt(payloadHexs[i], 16);
        }

        final int scanId = 1;
        final int sensorId = 1;
        final String timeZone = "Europe/Istanbul";
        final long timestampLocal = 1680559254630L;
        final long timestampUTC = 1680548454630L;
        final long rightCRC = 1928179073;

        boolean testPassed;

        try {

            RawScan rsr = new RawScan(patchInfo, payload, scanId, sensorId, timeZone, timestampLocal, timestampUTC);
            testPassed = rightCRC == rsr.CRC;
        } catch (IOException e) {
            testPassed = false;
        }
        return testPassed;
    }
}
