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

public class HistoricReadingTable {
    private final SQLiteDatabase db;

    private final LibreMessage libreMessage;
    private final SqliteSequence sqlseq;

    public HistoricReadingTable(SQLiteDatabase db, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.libreMessage = libreMessage;
        this.sqlseq = new SqliteSequence(db);
        if(SQLUtils.isTableNull(db, TableStrings.TABLE_NAME)){
            throw new Exception(String.format("%s table is null", TableStrings.TABLE_NAME));
        }

        // TODO: почему-то в LibreLink не отображается график истории
        this.testReadingOrWriting(SQLUtils.Mode.READING);
    }
    private void testReadingOrWriting(SQLUtils.Mode mode) throws Exception {
        this.fillClassByValuesInLastHistoricReadingRecord();
        long computedCRC = this.computeCRC32();
        long originalCRC = this.CRC;
        if(computedCRC != originalCRC){
            throw new Exception(String.format("%s table %s test is not passed.", TableStrings.TABLE_NAME, mode));
        }
    }

    private Integer getLastStoredReadingId(){
        return SQLUtils.getLastStoredFieldValue(db, TableStrings.readingId, TableStrings.TABLE_NAME);
    }

    private Object getRelatedValueForLastReadingId(String fieldName) {
        final Integer lastStoredReadingId = getLastStoredReadingId();
        return SQLUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.readingId, lastStoredReadingId);
    }

    private long computeCRC32() throws IOException {
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

    public void addLastSensorScan() throws Exception {
        int lastStoredSampleNumber = ((Long) this.getRelatedValueForLastReadingId(TableStrings.sampleNumber)).intValue();
        int lastStoredReadingId = ((Long) this.getRelatedValueForLastReadingId(TableStrings.readingId)).intValue();
        HistoricBg[] missingHistoricBgs = Arrays.stream(libreMessage.getHistoricBgs())
                .filter(bg -> bg.getSampleNumber() > lastStoredSampleNumber)
                .toArray(HistoricBg[]::new);

        for (HistoricBg missedHistoricBg : missingHistoricBgs){
            this.addNewRecord(missedHistoricBg, ++lastStoredReadingId);
        }
    }

    private void addNewRecord(HistoricBg historicBg, int readingId) throws Exception {
        this.glucoseValue = historicBg.convertBG(GlucoseUnit.MGDL).getBG();
        this.readingId = readingId;
        this.sampleNumber = historicBg.getSampleNumber();
        this.sensorId = SQLUtils.computeSensorId(this.db);
        this.timeChangeBefore = 0;
        this.timeZone = historicBg.getTimeZone();
        this.timestampLocal = historicBg.getTimestampLocal();
        this.timestampUTC = historicBg.getTimestampUTC();
        long computedCRC = this.computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.glucoseValue, glucoseValue);
        values.put(TableStrings.readingId, readingId);
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.timeChangeBefore, timeChangeBefore);
        values.put(TableStrings.timeZone, timeZone);
        values.put(TableStrings.timestampUTC, timestampUTC);
        values.put(TableStrings.timestampLocal, timestampLocal);
        values.put(TableStrings.CRC, computedCRC);

        db.insert(TableStrings.TABLE_NAME, null, values);
        this.onTableChanged();
    }

    private void onTableChanged() throws Exception {
        sqlseq.onNewRecordMade(TableStrings.TABLE_NAME);
        this.testReadingOrWriting(SQLUtils.Mode.WRITING);
    }

    public void fillClassByValuesInLastHistoricReadingRecord() {
        // That constructor for AppTester.java only
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

    private static class TableStrings{
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
