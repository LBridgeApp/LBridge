package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.oop1.GlucoseUnit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class RealTimeReadingTable implements CrcTable {
    private final SQLiteDatabase db;
    private final SensorTable sensorTable;
    private final LibreMessage libreMessage;

    public RealTimeReadingTable(SQLiteDatabase db, SensorTable sensorTable, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.sensorTable = sensorTable;
        this.libreMessage = libreMessage;

        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.READING);
    }

    private Integer getLastStoredReadingId() {
        return SqlUtils.getLastStoredFieldValue(db, TableStrings.readingId, TableStrings.TABLE_NAME);
    }

    private Object getRelatedValueForLastReadingId(String fieldName) {
        final Integer lastStoredReadingId = getLastStoredReadingId();
        return SqlUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.readingId, lastStoredReadingId);
    }

    private int computeAlarm() {
        /*
         * NOT_DETERMINED(0),
         * LOW_GLUCOSE(1),
         * PROJECTED_LOW_GLUCOSE(2),
         * GLUCOSE_OK(3),
         * PROJECTED_HIGH_GLUCOSE(4),
         * HIGH_GLUCOSE(5);
         */
        double bg = libreMessage.getCurrentBg().convertBG(GlucoseUnit.MMOL).getBG();
        if (bg < 3.9) {
            return 1;
        } else if (bg > 10.0) {
            return 5;
        } else {
            return 3;
        }
    }


    @Override
    public void fillByLastRecord() {
        this.alarm = ((Long) this.getRelatedValueForLastReadingId(TableStrings.alarm)).intValue();
        this.glucoseValue = ((Float) this.getRelatedValueForLastReadingId(TableStrings.glucoseValue)).doubleValue();
        this.isActionable = (long) this.getRelatedValueForLastReadingId(TableStrings.isActionable) != 0;
        this.rateOfChange = ((Float) this.getRelatedValueForLastReadingId(TableStrings.rateOfChange)).doubleValue();
        this.readingId = ((Long) this.getRelatedValueForLastReadingId(TableStrings.readingId)).intValue();
        this.sensorId = ((Long) this.getRelatedValueForLastReadingId(TableStrings.sensorId)).intValue();
        this.timeChangeBefore = (long) this.getRelatedValueForLastReadingId(TableStrings.timeChangeBefore);
        this.timeZone = (String) this.getRelatedValueForLastReadingId(TableStrings.timeZone);
        this.timestampUTC = (long) this.getRelatedValueForLastReadingId(TableStrings.timestampUTC);
        this.timestampLocal = (long) this.getRelatedValueForLastReadingId(TableStrings.timestampLocal);
        this.trendArrow = ((Long) this.getRelatedValueForLastReadingId(TableStrings.trendArrow)).intValue();
        this.CRC = (long) this.getRelatedValueForLastReadingId(TableStrings.CRC);
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

    @Override
    public long getOriginalCRC() {
        return this.CRC;
    }

    @Override
    public boolean isTableNull() {
        return SqlUtils.isTableNull(this.db, TableStrings.TABLE_NAME);
    }

    public void addLastSensorScan() throws Exception {
        this.alarm = this.computeAlarm();
        this.glucoseValue = libreMessage.getCurrentBg().convertBG(GlucoseUnit.MGDL).getBG();
        this.isActionable = true;
        this.rateOfChange = 0.0;
        // не нужно менять readingId, так как это значение само увеличивается при добавлении записи.
        //this.readingId = (this.getLastStoredReadingId() == null) ? 1 : this.getLastStoredReadingId() + 1;
        this.sensorId = sensorTable.getLastStoredSensorId();
        this.timeChangeBefore = 0;
        this.timeZone = libreMessage.getCurrentBg().getTimeZone();
        this.timestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        this.timestampUTC = libreMessage.getCurrentBg().getTimestampUTC();
        this.trendArrow = libreMessage.getCurrentBg().getCurrentTrend().toValue();
        long computedCRC = this.computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.alarm, alarm);
        values.put(TableStrings.glucoseValue, glucoseValue);
        values.put(TableStrings.isActionable, isActionable);
        values.put(TableStrings.rateOfChange, rateOfChange);
        // не нужно менять readingId, так как это значение само увеличивается при добавлении записи.
        //values.put(TableStrings.readingId, readingId);
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.timeChangeBefore, timeChangeBefore);
        values.put(TableStrings.timeZone, timeZone);
        values.put(TableStrings.timestampUTC, timestampUTC);
        values.put(TableStrings.timestampLocal, timestampLocal);
        values.put(TableStrings.trendArrow, trendArrow);
        values.put(TableStrings.CRC, computedCRC);

        db.insertOrThrow(TableStrings.TABLE_NAME, null, values);
        this.triggerOnTableChangedEvent();
    }

    private void triggerOnTableChangedEvent() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.WRITING);
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

    private long CRC;

    private static class TableStrings {
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
}
