package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.libre.LibreMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class RawScanTable implements CrcTable, TimeTable {
    private final SQLiteDatabase db;
    private final SensorTable sensorTable;
    private final LibreMessage libreMessage;

    public RawScanTable(SQLiteDatabase db, SensorTable sensorTable, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.sensorTable = sensorTable;
        this.libreMessage = libreMessage;

        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.READING);
    }

    private Integer getLastStoredScanId() {
        return SqlUtils.getLastStoredFieldValue(db, TableStrings.scanId, TableStrings.TABLE_NAME);
    }
    private Object getRelatedValueForLastScanId(String fieldName) {
        final Integer lastStoredScanId = getLastStoredScanId();
        return SqlUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.scanId, lastStoredScanId);
    }

    @Override
    public void fillByLastRecord() {
        this.patchInfo = (byte[]) this.getRelatedValueForLastScanId(TableStrings.patchInfo);
        this.payload = (byte[]) this.getRelatedValueForLastScanId(TableStrings.payload);
        this.scanId = ((Long) this.getRelatedValueForLastScanId(TableStrings.scanId)).intValue();
        this.sensorId = ((Long) this.getRelatedValueForLastScanId(TableStrings.sensorId)).intValue();
        this.timeZone = (String) this.getRelatedValueForLastScanId(TableStrings.timeZone);
        this.timestampUTC = (long) this.getRelatedValueForLastScanId(TableStrings.timestampUTC);
        this.timestampLocal = (long) this.getRelatedValueForLastScanId(TableStrings.timestampLocal);
        this.CRC = (long) this.getRelatedValueForLastScanId(TableStrings.CRC);
    }

    @Override
    public String getTableName() {
        return TableStrings.TABLE_NAME;
    }

    @Override
    public long computeCRC32() throws IOException {
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

    @Override
    public long getOriginalCRC() {
        return this.CRC;
    }

    @Override
    public long getLastUTCTimestamp() {
        return (long) this.getRelatedValueForLastScanId(TableStrings.timestampUTC);
    }

    @Override
    public boolean isTableNull() {
        return SqlUtils.isTableNull(this.db, TableStrings.TABLE_NAME);
    }

    public void addLastSensorScan() throws Exception {
        SqlUtils.validateTime(this, libreMessage);

        this.patchInfo = libreMessage.getRawLibreData().getPatchInfo();
        this.payload = libreMessage.getRawLibreData().getPayload();
        // не нужно менять scanId, так как это значение само увеличивается при добавлении записи.
        this.sensorId = sensorTable.getLastStoredSensorId();
        this.timeZone = libreMessage.getCurrentBg().getTimeZone();
        this.timestampUTC = libreMessage.getCurrentBg().getTimestampUTC();
        this.timestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        long computedCRC = this.computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.patchInfo, patchInfo);
        values.put(TableStrings.payload, payload);
        // не нужно менять scanId, так как это значение само увеличивается при добавлении записи.
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.timeZone, timeZone);
        values.put(TableStrings.timestampLocal, timestampLocal);
        values.put(TableStrings.timestampUTC, timestampUTC);
        values.put(TableStrings.CRC, computedCRC);

        db.insertOrThrow(TableStrings.TABLE_NAME, null, values);
        this.onTableChanged();
    }

    private void onTableChanged() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.WRITING);
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
