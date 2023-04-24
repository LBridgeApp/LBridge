package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.example.nfc_libre_scan.libre.LibreMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.zip.CRC32;

public class SensorTable implements CrcTable {

    private final SQLiteDatabase db;
    private final LibreMessage libreMessage;

    public SensorTable(SQLiteDatabase db, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.libreMessage = libreMessage;
        if(SqlUtils.isTableNull(db, TableStrings.TABLE_NAME)){
            throw new Exception(String.format("%s table is null", TableStrings.TABLE_NAME));
        }

        if(isSensorExpired()){
            throw new Exception(String.format("Sensor is expired in %s table. Clear database", TableStrings.TABLE_NAME));
        }

        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.READING);
    }

    private boolean isSensorExpired() {
        this.sensorStartTimestampUTC = (long) this.getRelatedValueForLastSensorId(TableStrings.getSensorStartTimestampUTC);

        Instant startInstant = Instant.ofEpochMilli(this.sensorStartTimestampUTC);
        Instant endInstant = startInstant.plus(14, ChronoUnit.DAYS);
        long sensorExpirationTimestamp = endInstant.toEpochMilli();

        long currentTimestamp = System.currentTimeMillis();

        return currentTimestamp >= sensorExpirationTimestamp;
    }


    protected Integer getLastStoredSensorId() {
        return SqlUtils.getLastStoredFieldValue(db, TableStrings.sensorId, TableStrings.TABLE_NAME);
    }

    private Object getRelatedValueForLastSensorId(String fieldName) {
        final int lastStoredSensorId = getLastStoredSensorId();
        return SqlUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.sensorId, lastStoredSensorId);
    }

    public void updateToLastScan() throws Exception {
        this.sensorId = getLastStoredSensorId();
        this.attenuationState = libreMessage.getLibreSavedState().getAttenuationState();
        this.compositeState = libreMessage.getLibreSavedState().getCompositeState();
        this.lastScanSampleNumber = libreMessage.getCurrentBg().getSampleNumber();
        this.lastScanTimeZone = libreMessage.getCurrentBg().getTimeZone();
        this.lastScanTimestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        this.lastScanTimestampUTC = libreMessage.getCurrentBg().getTimestampUTC();
        long computedCRC = this.computeCRC32();

        String sql = String.format("UPDATE %s SET %s=%s, %s=%s, %s=%s, %s=%s, %s=?, %s=?, %s=%s WHERE %s=%s;",
                TableStrings.TABLE_NAME,
                TableStrings.lastScanSampleNumber, lastScanSampleNumber,
                TableStrings.lastScanTimeZone, DatabaseUtils.sqlEscapeString(lastScanTimeZone),
                TableStrings.lastScanTimestampLocal, lastScanTimestampLocal,
                TableStrings.lastScanTimestampUTC, lastScanTimestampUTC,
                TableStrings.attenuationState,
                TableStrings.compositeState,
                TableStrings.CRC, computedCRC,
                TableStrings.sensorId, this.sensorId);

        SQLiteStatement statement = db.compileStatement(sql);

        if (attenuationState != null) {
            statement.bindBlob(1, attenuationState);
        } else {
            statement.bindNull(1);
        }

        if (compositeState != null) {
            statement.bindBlob(2, compositeState);
        } else {
            statement.bindNull(2);
        }

        statement.execute();

        this.triggerOnTableChangedEvent();
    }

    private void triggerOnTableChangedEvent() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.WRITING);
    }

    @Override
    public void fillByLastRecord() {
        this.attenuationState = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.attenuationState);
        this.bleAddress = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.bleAddress);
        this.compositeState = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.compositeState);
        this.enableStreamingTimestamp = ((Long) this.getRelatedValueForLastSensorId(TableStrings.enableStreamingTimestamp)).intValue();
        this.endedEarly = (long) this.getRelatedValueForLastSensorId(TableStrings.endedEarly) != 0;
        this.initialPatchInformation = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.initialPatchInformation);
        this.lastScanSampleNumber = ((Long) this.getRelatedValueForLastSensorId(TableStrings.lastScanSampleNumber)).intValue();
        this.lastScanTimeZone = (String) this.getRelatedValueForLastSensorId(TableStrings.lastScanTimeZone);
        this.lastScanTimestampLocal = (long) this.getRelatedValueForLastSensorId(TableStrings.lastScanTimestampLocal);
        this.lastScanTimestampUTC = (long) this.getRelatedValueForLastSensorId(TableStrings.lastScanTimestampUTC);
        this.lsaDetected = (long) this.getRelatedValueForLastSensorId(TableStrings.lsaDetected) != 0;
        this.measurementState = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.measurementState);
        this.personalizationIndex = ((Long) this.getRelatedValueForLastSensorId(TableStrings.personalizationIndex)).intValue();
        this.sensorId = ((Long) this.getRelatedValueForLastSensorId(TableStrings.sensorId)).intValue();
        this.sensorStartTimeZone = (String) this.getRelatedValueForLastSensorId(TableStrings.sensorStartTimeZone);
        this.sensorStartTimestampLocal = (long) this.getRelatedValueForLastSensorId(TableStrings.sensorStartTimestampLocal);
        this.sensorStartTimestampUTC = (long) this.getRelatedValueForLastSensorId(TableStrings.getSensorStartTimestampUTC);
        this.serialNumber = (String) this.getRelatedValueForLastSensorId(TableStrings.serialNumber);
        this.streamingAuthenticationData = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.streamingAuthenticationData);
        this.streamingUnlockCount = ((Long) this.getRelatedValueForLastSensorId(TableStrings.streamingUnlockCount)).intValue();
        this.uniqueIdentifier = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.uniqueIdentifier);
        this.unrecordedHistoricTimeChange = (long) this.getRelatedValueForLastSensorId(TableStrings.unrecordedHistoricTimeChange);
        this.unrecordedRealTimeTimeChange = (long) this.getRelatedValueForLastSensorId(TableStrings.unrecordedRealTimeTimeChange);
        this.userId = ((Long) this.getRelatedValueForLastSensorId(TableStrings.userId)).intValue();
        this.warmupPeriodInMinutes = ((Long) this.getRelatedValueForLastSensorId(TableStrings.warmupPeriodInMinutes)).intValue();
        this.wearDurationInMinutes = ((Long) this.getRelatedValueForLastSensorId(TableStrings.wearDurationInMinutes)).intValue();
        this.CRC = (long) this.getRelatedValueForLastSensorId(TableStrings.CRC);
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

        dataOutputStream.writeInt(this.userId);
        dataOutputStream.writeUTF(this.serialNumber);
        dataOutputStream.write(this.uniqueIdentifier);
        dataOutputStream.writeInt(this.personalizationIndex);
        dataOutputStream.write(this.initialPatchInformation);
        dataOutputStream.writeInt(this.enableStreamingTimestamp);
        dataOutputStream.writeInt(this.streamingUnlockCount);
        dataOutputStream.writeLong(this.sensorStartTimestampUTC);
        dataOutputStream.writeLong(this.sensorStartTimestampLocal);
        dataOutputStream.writeUTF(this.sensorStartTimeZone);
        dataOutputStream.writeLong(this.lastScanTimestampUTC);
        dataOutputStream.writeLong(this.lastScanTimestampLocal);
        dataOutputStream.writeUTF(this.lastScanTimeZone);
        dataOutputStream.writeInt(this.lastScanSampleNumber);
        dataOutputStream.writeBoolean(this.endedEarly);
        byte[] compositeState = this.compositeState;
        if (compositeState != null) {
            dataOutputStream.write(compositeState);
        }
        byte[] attenuationState = this.attenuationState;
        if (attenuationState != null) {
            dataOutputStream.write(attenuationState);
        }
        byte[] measurementState = this.measurementState;
        if (measurementState != null) {
            dataOutputStream.write(measurementState);
        }
        dataOutputStream.writeBoolean(this.lsaDetected);
        dataOutputStream.writeLong(this.unrecordedHistoricTimeChange);
        dataOutputStream.writeLong(this.unrecordedRealTimeTimeChange);
        dataOutputStream.writeInt(this.warmupPeriodInMinutes);
        dataOutputStream.writeInt(this.wearDurationInMinutes);
        byte[] streamingAuthenticationData = this.streamingAuthenticationData;
        if (streamingAuthenticationData != null) {
            dataOutputStream.write(streamingAuthenticationData);
        }
        byte[] bleAddress = this.bleAddress;
        if (bleAddress != null) {
            dataOutputStream.write(bleAddress);
        }

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

    private byte[] attenuationState;
    private byte[] bleAddress;
    private byte[] compositeState;
    private int enableStreamingTimestamp;
    private boolean endedEarly;
    private byte[] initialPatchInformation;
    private int lastScanSampleNumber;
    private String lastScanTimeZone;
    private long lastScanTimestampLocal;
    private long lastScanTimestampUTC;
    private boolean lsaDetected;
    private byte[] measurementState;
    private int personalizationIndex;
    private int sensorId;
    private String sensorStartTimeZone;
    private long sensorStartTimestampLocal;
    private long sensorStartTimestampUTC;
    private String serialNumber;
    private byte[] streamingAuthenticationData;
    private int streamingUnlockCount;
    private byte[] uniqueIdentifier;
    private long unrecordedHistoricTimeChange;
    private long unrecordedRealTimeTimeChange;
    private int userId;
    private int warmupPeriodInMinutes;
    private int wearDurationInMinutes;
    private long CRC;

    private static class TableStrings {
        final static String TABLE_NAME = "sensors";
        final static String attenuationState = "attenuationState";
        final static String bleAddress = "bleAddress";
        final static String compositeState = "compositeState";
        final static String enableStreamingTimestamp = "enableStreamingTimestamp";
        final static String endedEarly = "endedEarly";
        final static String initialPatchInformation = "initialPatchInformation";
        final static String lastScanSampleNumber = "lastScanSampleNumber";
        final static String lastScanTimeZone = "lastScanTimeZone";
        final static String lastScanTimestampLocal = "lastScanTimestampLocal";
        final static String lastScanTimestampUTC = "lastScanTimestampUTC";
        final static String lsaDetected = "lsaDetected";
        final static String measurementState = "measurementState";
        final static String personalizationIndex = "personalizationIndex";
        final static String sensorId = "sensorId";
        final static String sensorStartTimeZone = "sensorStartTimeZone";
        final static String sensorStartTimestampLocal = "sensorStartTimestampLocal";
        final static String getSensorStartTimestampUTC = "sensorStartTimestampUTC";
        final static String serialNumber = "serialNumber";
        final static String streamingAuthenticationData = "streamingAuthenticationData";
        final static String streamingUnlockCount = "streamingUnlockCount";
        final static String uniqueIdentifier = "uniqueIdentifier";
        final static String unrecordedHistoricTimeChange = "unrecordedHistoricTimeChange";
        final static String unrecordedRealTimeTimeChange = "unrecordedRealTimeTimeChange";
        final static String userId = "userId";
        final static String warmupPeriodInMinutes = "warmupPeriodInMinutes";
        final static String wearDurationInMinutes = "wearDurationInMinutes";
        final static String CRC = "CRC";
    }
}
