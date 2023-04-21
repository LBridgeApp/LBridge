package com.example.nfc_libre_scan.librelink_sas_db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.libre.LibreMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class RawScanTable {
    private final SqliteSequence sqlseq;
    private final SQLiteDatabase db;
    private final LibreMessage libreMessage;

    public RawScanTable(SQLiteDatabase db, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.libreMessage = libreMessage;
        this.sqlseq = new SqliteSequence(db);
        if(SQLUtils.isTableNull(db, TableStrings.TABLE_NAME)){
            throw new Exception(String.format("%s table is null", TableStrings.TABLE_NAME));
        }

        this.testReadingOrWriting(SQLUtils.Mode.READING);
    }

    private void testReadingOrWriting(SQLUtils.Mode mode) throws Exception {
        this.fillClassByValuesInLastRawScanRecord();
        long computedCRC = this.computeCRC32();
        long originalCRC = this.CRC;
        if(computedCRC != originalCRC){
            throw new Exception(String.format("%s table %s test is not passed.", TableStrings.TABLE_NAME, mode));
        }
    }

    private Integer getLastStoredScanId() {
        return SQLUtils.getLastStoredFieldValue(db, TableStrings.scanId, TableStrings.TABLE_NAME);
    }
    private Object getRelatedValueForLastScanId(String fieldName) {
        final Integer lastStoredScanId = getLastStoredScanId();
        return SQLUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.scanId, lastStoredScanId);
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

    public void addNewSensorScan() throws Exception {
        this.patchInfo = libreMessage.getRawLibreData().getPatchInfo();
        this.payload = libreMessage.getRawLibreData().getPayload();
        this.scanId = getLastStoredScanId() + 1;
        this.sensorId = SQLUtils.computeSensorId(this.db);
        this.timeZone = libreMessage.getCurrentBg().getTimeZone();
        this.timestampUTC = libreMessage.getCurrentBg().getTimestampUTC();
        this.timestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
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
        this.onTableChanged();
    }

    private void onTableChanged() throws Exception {
        sqlseq.onNewRecordMade(TableStrings.TABLE_NAME);
        this.testReadingOrWriting(SQLUtils.Mode.WRITING);
    }

    public void fillClassByValuesInLastRawScanRecord() {
        // That constructor for AppTester.java only

        this.patchInfo = (byte[]) this.getRelatedValueForLastScanId(TableStrings.patchInfo);

        this.payload = (byte[]) this.getRelatedValueForLastScanId(TableStrings.payload);

        this.scanId = ((Long) this.getRelatedValueForLastScanId(TableStrings.scanId)).intValue();

        this.sensorId = ((Long) this.getRelatedValueForLastScanId(TableStrings.sensorId)).intValue();

        this.timeZone = (String) this.getRelatedValueForLastScanId(TableStrings.timeZone);

        this.timestampUTC = (long) this.getRelatedValueForLastScanId(TableStrings.timestampUTC);

        this.timestampLocal = (long) this.getRelatedValueForLastScanId(TableStrings.timestampLocal);

        this.CRC = (long) this.getRelatedValueForLastScanId(TableStrings.CRC);
    }

    private byte[] patchInfo;
    private byte[] payload;
    private int scanId;
    private int sensorId;
    private String timeZone;
    private long timestampLocal;
    private long timestampUTC;

    private long CRC;

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
}
