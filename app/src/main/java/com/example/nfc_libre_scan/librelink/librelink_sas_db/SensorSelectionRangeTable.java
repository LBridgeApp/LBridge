package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;

import com.example.nfc_libre_scan.libre.LibreMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class SensorSelectionRangeTable implements CrcTable, TimeTable {

    private final LibreLinkDatabase db;
    private final SensorTable sensorTable;
    private final LibreMessage libreMessage;

    SensorSelectionRangeTable(LibreLinkDatabase db) throws Exception {
        this.db = db;
        this.sensorTable = db.getSensorTable();
        this.libreMessage = db.getLibreMessage();

        this.onTableClassInit();
    }
    @Override
    public void onTableClassInit() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.READING);
    }

    @Override
    public void fillByLastRecord() {
        this.endTimestampUTC = (Long) this.getRelatedValueForLastSensorId(TableStrings.endTimestampUTC);
        this.rangeId = ((Long)this.getRelatedValueForLastSensorId(TableStrings.rangeId)).intValue();
        this.sensorId = ((Long) this.getRelatedValueForLastSensorId(TableStrings.sensorId)).intValue();
        this.startTimestampUTC = (Long) this.getRelatedValueForLastSensorId(TableStrings.startTimestampUTC);
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
        dataOutputStream.writeLong(this.startTimestampUTC);
        dataOutputStream.writeLong(this.endTimestampUTC);

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
        return (long) this.getRelatedValueForLastSensorId(TableStrings.startTimestampUTC);
    }

    @Override
    public boolean isTableNull() {
        return SqlUtils.isTableNull(this.db.getObject(), TableStrings.TABLE_NAME);
    }

    @Override
    public void onTableChanged() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.WRITING);
    }

    private Object getRelatedValueForLastSensorId(String fieldName) {
        final Integer lastStoredSensorId = sensorTable.getLastStoredSensorId();
        return SqlUtils.getRelatedValue(db.getObject(), fieldName, TableStrings.TABLE_NAME, TableStrings.sensorId, lastStoredSensorId);
    }

    public void createNewSensorRecord() throws Exception {
        SqlUtils.validateTime(this, libreMessage);

        // не знаю почему, но в таблице LibreLink
        // это значение указано как 9223372036854775807
        this.endTimestampUTC = Long.MAX_VALUE;
        this.rangeId = 1;
        this.sensorId = sensorTable.getLastStoredSensorId();
        this.startTimestampUTC = libreMessage.getSensorStartTimestampUTC();
        this.CRC = computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.endTimestampUTC, endTimestampUTC);
        values.put(TableStrings.rangeId, rangeId);
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.startTimestampUTC, startTimestampUTC);
        values.put(TableStrings.CRC, CRC);

        db.getObject().insertOrThrow(TableStrings.TABLE_NAME, null, values);
        this.onTableChanged();
    }

    private long endTimestampUTC;
    private int rangeId;
    private int sensorId;
    private long startTimestampUTC;
    private long CRC;

    private static class TableStrings {
        final static String TABLE_NAME = "sensorSelectionRanges";
        final static String endTimestampUTC = "endTimestampUTC";
        final static String rangeId = "rangeId";
        final static String sensorId = "sensorId";
        final static String startTimestampUTC = "startTimestampUTC";
        final static String CRC = "CRC";
    }
}
