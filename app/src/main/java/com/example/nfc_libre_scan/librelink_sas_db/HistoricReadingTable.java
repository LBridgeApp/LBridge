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
    private Integer lastStoredReadingId;

    public HistoricReadingTable(SQLiteDatabase db, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.libreMessage = libreMessage;
        this.sqlseq = new SqliteSequence(db);
        if(GeneralUtils.isTableNull(db, TableStrings.TABLE_NAME)){
            throw new Exception("Table is null");
        }
    }

    private Integer getLastStoredReadingId() {
        if(lastStoredReadingId == null){
            lastStoredReadingId = GeneralUtils.getLastStoredFieldValue(db, TableStrings.readingId, TableStrings.TABLE_NAME);
        }
        return lastStoredReadingId;
    }
    private Object getRelatedValueForLastReadingId(String fieldName) {
        final int lastStoredReadingId = getLastStoredReadingId();
        return GeneralUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.readingId, lastStoredReadingId);
    }

    public long getComputedCRC() throws IOException { return computeCRC32(); }

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

    public void addLastSensorScan() throws IOException {
        int lastStoredSampleNumber = this.getSampleNumber();
        int lastStoredReadingId = this.getReadingId();
        HistoricBg[] missingHistoricBgs = Arrays.stream(libreMessage.getHistoricBgArray())
                .filter(bg -> bg.getSampleNumber() > lastStoredSampleNumber)
                .toArray(HistoricBg[]::new);

        for (HistoricBg missedHistoricBg : missingHistoricBgs){
            this.addNewRecord(missedHistoricBg, ++lastStoredReadingId);
        }
    }

    private void addNewRecord(HistoricBg historicBg, int readingId) throws IOException {
        this.glucoseValue = historicBg.convertBG(GlucoseUnit.MGDL).getBG();
        this.readingId = readingId;
        this.sampleNumber = historicBg.getSampleNumber();
        this.sensorId = GeneralUtils.computeSensorId(this.db);
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
        sqlseq.onNewRecordMade(TableStrings.TABLE_NAME);
    }
    private double glucoseValue;
    private int readingId;
    private int sampleNumber;
    private int sensorId;
    private long timeChangeBefore;
    private String timeZone;
    private long timestampLocal;
    private long timestampUTC;

    private double getGlucoseValue(){
        Float f = (Float) this.getRelatedValueForLastReadingId(TableStrings.glucoseValue);
        return f.doubleValue();
    }

    private int getReadingId(){
        Long l = (Long) this.getRelatedValueForLastReadingId(TableStrings.readingId);
        return l.intValue();
    }

    private int getSampleNumber(){
        Long l = (Long) this.getRelatedValueForLastReadingId(TableStrings.sampleNumber);
        return l.intValue();
    }

    private int getSensorId(){
        Long l = (Long) this.getRelatedValueForLastReadingId(TableStrings.sensorId);
        return l.intValue();
    }

    private long getTimeChangeBefore(){
        return (long) this.getRelatedValueForLastReadingId(TableStrings.timeChangeBefore);
    }

    private String getTimeZone(){
        return (String) this.getRelatedValueForLastReadingId(TableStrings.timeZone);
    }

    private long getTimestampLocal(){
        return (long) this.getRelatedValueForLastReadingId(TableStrings.timestampLocal);
    }

    private long getTimestampUTC(){
        return (long) this.getRelatedValueForLastReadingId(TableStrings.timestampUTC);
    }

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

    public void fillClassByValuesInLastHistoricReadingRecord() {
        // That constructor for AppTester.java only
        this.glucoseValue = getGlucoseValue();
        this.readingId = getReadingId();
        this.sampleNumber = getSampleNumber();
        this.sensorId = getSensorId();
        this.timeChangeBefore = getTimeChangeBefore();
        this.timeZone = getTimeZone();
        this.timestampUTC = getTimestampUTC();
        this.timestampLocal = getTimestampLocal();
    }
}
