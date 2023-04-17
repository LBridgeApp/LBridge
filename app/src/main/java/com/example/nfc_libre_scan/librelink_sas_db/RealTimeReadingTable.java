package com.example.nfc_libre_scan.librelink_sas_db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class RealTimeReadingTable {
    private final SQLiteDatabase db;
    private final SqliteSequence sqlseq;
    private Integer lastReadingIdForSearch;

    public RealTimeReadingTable(SQLiteDatabase db){
        this.db = db;
        sqlseq = new SqliteSequence(db);
    }

    private Integer getLastReadingIdForSearch() {
        if(lastReadingIdForSearch == null){
            lastReadingIdForSearch = GeneralUtils.getLastFieldValueForSearch(db, TableStrings.readingId, TableStrings.TABLE_NAME);
        }
        return lastReadingIdForSearch;
    }
    private Object getRelatedValueForLastReadingId(String fieldName) {
        final int lastReadingIdForSearch = getLastReadingIdForSearch();
        return GeneralUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.readingId, lastReadingIdForSearch);
    }

    public long getComputedCRC() throws IOException { return computeCRC32(); }

    private String computeTimeZone() {
        return GeneralUtils.computeCurrentTimeZone();
    }

    private long computeTimestampLocal() {
        return GeneralUtils.computeTimestampLocal(this.timestampUTC);
    }

    private long computeTimestampUTC() {
        return GeneralUtils.computeTimestampUTC();
    }

    private long computeCRC32() throws IOException {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.sensorId);
        dataOutputStream.writeLong(this.timestampUTC);
        dataOutputStream.writeLong(this.timestampLocal);
        dataOutputStream.writeUTF(this.timeZone);
        dataOutputStream.writeDouble(this.glucoseValue);
        dataOutputStream.writeDouble(this.rateOfChange);
        dataOutputStream.writeInt(this.trendArrow);
        dataOutputStream.writeInt(this.alarm);
        dataOutputStream.writeBoolean(this.isActionable);
        dataOutputStream.writeLong(this.timeChangeBefore);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    public void addNewRecord() throws IOException {
        this.alarm = computeAlarm();
        this.glucoseValue = computeGlucoseValue();
        this.isActionable = computeIsActionable();
        this.rateOfChange = computeRateOfChange();
        this.readingId = computeReadingId();
        this.sensorId = computeSensorId();
        this.timeChangeBefore = computeTimeChangeBefore();
        this.timeZone = computeTimeZone();
        this.timestampLocal = computeTimestampLocal();
        this.timestampUTC = computeTimestampUTC();
        this.trendArrow = computeTrendArrow();
        long computedCRC = computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.alarm, alarm);
        values.put(TableStrings.glucoseValue, glucoseValue);
        values.put(TableStrings.isActionable, isActionable);
        values.put(TableStrings.rateOfChange, rateOfChange);
        values.put(TableStrings.readingId, readingId);
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.timeChangeBefore, timeChangeBefore);
        values.put(TableStrings.timeZone, timeZone);
        values.put(TableStrings.timestampUTC, timestampUTC);
        values.put(TableStrings.timestampLocal, timestampLocal);
        values.put(TableStrings.trendArrow, trendArrow);
        values.put(TableStrings.CRC, computedCRC);

        db.insert(TableStrings.TABLE_NAME, null, values);
        sqlseq.onNewRecord(TableStrings.TABLE_NAME);
    }

    private int alarm;
    private double glucoseValue;
    private boolean isActionable;
    private double rateOfChange;
    private int readingId;
    private int sensorId;
    private long timeChangeBefore;
    private String timeZone;
    private long timestampLocal;
    private long timestampUTC;
    private int trendArrow;

    private static class TableStrings{
        final static String TABLE_NAME = "RealTimeReadings";
        final static String alarm = "alarm";
        final static String glucoseValue = "glucoseValue";
        final static String isActionable = "isActionable";
        final static String rateOfChange = "rateOfChange";
        final static String readingId = "ReadingId";
        final static String sensorId = "sensorId";
        final static String timeChangeBefore = "timeChangeBefore";
        final static String timeZone = "timeZone";
        final static String timestampLocal = "timestampLocal";
        final static String timestampUTC = "timestampUTC";
        final static String trendArrow = "trendArrow";
        final static String CRC = "CRC";
    }

    private int getAlarm(){
        Long l = (Long) this.getRelatedValueForLastReadingId(TableStrings.alarm);
        return l.intValue();
    }

    private double getGlucoseValue(){
        Float f = (Float) this.getRelatedValueForLastReadingId(TableStrings.glucoseValue);
        return f.doubleValue();
    }

    private boolean getIsActionable(){
        return (long) this.getRelatedValueForLastReadingId(TableStrings.isActionable) != 0;
    }

    private double getRateOfChange(){
        Float f = (Float) this.getRelatedValueForLastReadingId(TableStrings.rateOfChange);
        return f.doubleValue();
    }

    private int getReadingId(){
        Long l = (Long) this.getRelatedValueForLastReadingId(TableStrings.readingId);
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

    private int getTrendArrow(){
        Long l = (Long) this.getRelatedValueForLastReadingId(TableStrings.trendArrow);
        return l.intValue();
    }

    public void fillClassByValuesInLastRealTimeReadingRecord() {
        // That constructor for AppTester.java only
        this.alarm = getAlarm();
        this.glucoseValue = getGlucoseValue();
        this.isActionable = getIsActionable();
        this.rateOfChange = getRateOfChange();
        this.readingId = getReadingId();
        this.sensorId = getSensorId();
        this.timeChangeBefore = getTimeChangeBefore();
        this.timeZone = getTimeZone();
        this.timestampUTC = getTimestampUTC();
        this.timestampLocal = getTimestampLocal();
        this.trendArrow = getTrendArrow();
    }
}
