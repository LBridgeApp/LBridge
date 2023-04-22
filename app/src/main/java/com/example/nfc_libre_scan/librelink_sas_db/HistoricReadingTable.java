package com.example.nfc_libre_scan.librelink_sas_db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.oop1.GlucoseUnit;
import com.oop1.HistoricBg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.CRC32;

public class HistoricReadingTable implements CRCable {
    private final SQLiteDatabase db;
    private final SensorTable sensorTable;
    private final LibreMessage libreMessage;
    private final SqliteSequence sqlseq;

    public HistoricReadingTable(SQLiteDatabase db, SensorTable sensorTable, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.libreMessage = libreMessage;
        this.sensorTable = sensorTable;
        this.sqlseq = new SqliteSequence(db);
        if (SqlUtils.isTableNull(db, TableStrings.TABLE_NAME)) {
            throw new Exception(String.format("%s table is null", TableStrings.TABLE_NAME));
        }

        SqlUtils.testReadingOrWriting(this, SqlUtils.Mode.READING);
    }

    private Integer getLastStoredReadingId() {
        return SqlUtils.getLastStoredFieldValue(db, TableStrings.readingId, TableStrings.TABLE_NAME);
    }

    private Object getRelatedValueForLastReadingId(String fieldName) {
        final Integer lastStoredReadingId = getLastStoredReadingId();
        return SqlUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.readingId, lastStoredReadingId);
    }

    @Override
    public void fillClassRelatedToLastFieldValueRecord() {
        this.fillClassByValuesInLastHistoricReadingRecord();
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
        dataOutputStream.writeInt(this.sampleNumber);
        dataOutputStream.writeLong(this.timestampUTC);
        dataOutputStream.writeLong(this.timestampLocal);
        dataOutputStream.writeUTF(this.timeZone);
        dataOutputStream.writeDouble(this.glucoseValue);
        dataOutputStream.writeLong(this.timeChangeBefore);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    @Override
    public long getOriginalCRC() {
        return this.CRC;
    }

    public void addLastSensorScan() throws Exception {
        int lastStoredSampleNumber = ((Long) this.getRelatedValueForLastReadingId(TableStrings.sampleNumber)).intValue();
        int lastStoredReadingId = ((Long) this.getRelatedValueForLastReadingId(TableStrings.readingId)).intValue();
        HistoricBg[] missingHistoricBgs = Arrays.stream(libreMessage.getHistoricBgs())
                .filter(bg -> bg.getSampleNumber() > lastStoredSampleNumber)
                .toArray(HistoricBg[]::new);

        for (HistoricBg missedHistoricBg : missingHistoricBgs) {
            this.addNewRecord(missedHistoricBg, ++lastStoredReadingId);
        }
    }

    private void addNewRecord(HistoricBg historicBg, int readingId) throws Exception {
        this.fillClassByValuesInLastHistoricReadingRecord();
        this.glucoseValue = historicBg.convertBG(GlucoseUnit.MGDL).getBG();
        this.readingId = readingId;
        this.sampleNumber = historicBg.getSampleNumber();
        this.sensorId = sensorTable.getLastStoredSensorId();
        this.timeChangeBefore = 0;
        this.timeZone = historicBg.getTimeZone();
        this.timestampLocal = historicBg.getTimestampLocal();
        this.timestampUTC = historicBg.getTimestampUTC();
        long computedCRC = this.computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.glucoseValue, glucoseValue);
        values.put(TableStrings.readingId, this.readingId);
        values.put(TableStrings.sampleNumber, this.sampleNumber);
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.timeChangeBefore, timeChangeBefore);
        values.put(TableStrings.timeZone, timeZone);
        values.put(TableStrings.timestampUTC, timestampUTC);
        values.put(TableStrings.timestampLocal, timestampLocal);
        values.put(TableStrings.CRC, computedCRC);

        db.insertOrThrow(TableStrings.TABLE_NAME, null, values);
        this.onTableChanged();
    }

    private void onTableChanged() throws Exception {
        sqlseq.onNewRecordMade(TableStrings.TABLE_NAME);
        SqlUtils.testReadingOrWriting(this, SqlUtils.Mode.WRITING);
    }

    public void fillClassByValuesInLastHistoricReadingRecord() {

        this.glucoseValue = ((Float) this.getRelatedValueForLastReadingId(TableStrings.glucoseValue)).doubleValue();
        this.readingId = ((Long) this.getRelatedValueForLastReadingId(TableStrings.readingId)).intValue();
        this.sampleNumber = ((Long) this.getRelatedValueForLastReadingId(TableStrings.sampleNumber)).intValue();
        this.sensorId = ((Long) this.getRelatedValueForLastReadingId(TableStrings.sensorId)).intValue();
        this.timeChangeBefore = (long) this.getRelatedValueForLastReadingId(TableStrings.timeChangeBefore);
        this.timeZone = (String) this.getRelatedValueForLastReadingId(TableStrings.timeZone);
        this.timestampUTC = (long) this.getRelatedValueForLastReadingId(TableStrings.timestampUTC);
        this.timestampLocal = (long) this.getRelatedValueForLastReadingId(TableStrings.timestampLocal);
        this.CRC = (long) this.getRelatedValueForLastReadingId(TableStrings.CRC);
    }

    private double glucoseValue;
    private int readingId;
    private int sampleNumber;
    private int sensorId;
    private long timeChangeBefore;
    private String timeZone;
    private long timestampLocal;
    private long timestampUTC;
    private long CRC;

    private static class TableStrings {
        final static String TABLE_NAME = "historicReadings";
        final static String glucoseValue = "glucoseValue";
        final static String readingId = "readingId";
        final static String sampleNumber = "sampleNumber";
        final static String sensorId = "sensorId";
        final static String timeChangeBefore = "timeChangeBefore";
        final static String timeZone = "timeZone";
        final static String timestampLocal = "timestampLocal";
        final static String timestampUTC = "timestampUTC";
        final static String CRC = "CRC";
    }
}
