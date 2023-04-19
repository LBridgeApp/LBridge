package com.example.nfc_libre_scan.librelink_sas_db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.oop1.CurrentBg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class RawScanTable {
    private final SqliteSequence sqlseq;
    private final SQLiteDatabase db;
    private final LibreMessage libreMessage;
    private Integer lastStoredScanId;

    public RawScanTable(SQLiteDatabase db, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.libreMessage = libreMessage;
        this.sqlseq = new SqliteSequence(db);
        if(GeneralUtils.isTableNull(db, TableStrings.TABLE_NAME)){
            throw new Exception("Table is null");
        }
    }

    private Integer getLastStoredScanId() {
        if(lastStoredScanId == null){
            lastStoredScanId = GeneralUtils.getLastStoredFieldValue(db, TableStrings.scanId, TableStrings.TABLE_NAME);
        }
        return lastStoredScanId;
    }
    private Object getRelatedValueForLastScanId(String fieldName) {
        final int lastStoredScanId = getLastStoredScanId();
        return GeneralUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.scanId, lastStoredScanId);
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

    public void addNewSensorScan() throws IOException {
        this.patchInfo = libreMessage.getPatchInfo();
        this.payload = libreMessage.getPayload();
        this.scanId = getLastStoredScanId() + 1;
        this.sensorId = GeneralUtils.computeSensorId(this.db);
        this.timeZone = libreMessage.getCurrentBgObject().getTimeZone();
        this.timestampUTC = libreMessage.getCurrentBgObject().getTimestampUTC();
        this.timestampLocal = libreMessage.getCurrentBgObject().getTimestampLocal();
        long computedCRC = this.computeCRC32();

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
        sqlseq.onNewRecordMade(TableStrings.TABLE_NAME);
    }

    private byte[] patchInfo;
    private byte[] payload;
    private int scanId;
    private int sensorId;
    private String timeZone;
    private long timestampLocal;
    private long timestampUTC;

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
