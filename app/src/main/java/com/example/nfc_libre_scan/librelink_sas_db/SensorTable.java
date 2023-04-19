package com.example.nfc_libre_scan.librelink_sas_db;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.oop1.CurrentBg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

public class SensorTable {

    private final SQLiteDatabase db;

    private final LibreMessage libreMessage;
    private Integer lastStoredSensorId;

    public SensorTable(SQLiteDatabase db, LibreMessage libreMessage) throws Exception {
        this.db = db;
        this.libreMessage = libreMessage;
        if(GeneralUtils.isTableNull(db, TableStrings.TABLE_NAME)){
            throw new Exception("Table is null");
        }
    }

    public boolean isSensorExpired(){
        long diffMillis = libreMessage.getCurrentBgObject().getTimestampUTC() - this.getSensorStartTimestampUTC();
        long diffDays = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS);
        return diffDays >= 14;
    }

    private Integer getLastStoredSensorId() {
        if (this.lastStoredSensorId == null) {
            this.lastStoredSensorId = GeneralUtils.getLastStoredFieldValue(db, TableStrings.sensorId, TableStrings.TABLE_NAME);
        }
        return lastStoredSensorId;
    }

    private Object getRelatedValueForLastSensorId(String fieldName) {
        final int lastStoredSensorId = getLastStoredSensorId();
        return GeneralUtils.getRelatedValue(db, fieldName, TableStrings.TABLE_NAME, TableStrings.sensorId, lastStoredSensorId);
    }

    public void updateToLastScan() throws IOException {
        this.fillClassByValuesInLastSensorRecord();
        this.lastScanSampleNumber = libreMessage.getCurrentBgObject().getSampleNumber();
        this.lastScanTimeZone = libreMessage.getCurrentBgObject().getTimeZone();
        this.lastScanTimestampLocal = libreMessage.getCurrentBgObject().getTimestampLocal();
        this.lastScanTimestampUTC = libreMessage.getCurrentBgObject().getTimestampUTC();
        final long computedCRC = this.computeCRC32();

        final String sql = String.format("UPDATE %s SET %s=%s, %s=%s, %s=%s, %s=%s, %s=%s WHERE %s=%s;",
                TableStrings.TABLE_NAME,
                TableStrings.lastScanSampleNumber, lastScanSampleNumber,
                TableStrings.lastScanTimeZone, DatabaseUtils.sqlEscapeString(lastScanTimeZone),
                TableStrings.lastScanTimestampLocal, lastScanTimestampLocal,
                TableStrings.lastScanTimestampUTC, lastScanTimestampUTC,
                TableStrings.CRC, computedCRC,
                TableStrings.sensorId, this.sensorId);
        db.execSQL(sql);
    }

    public long getComputedCRC() throws IOException {
        return computeCRC32();
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

    private byte[] getAttenuationState() {
        return (byte[]) this.getRelatedValueForLastSensorId(TableStrings.attenuationState);
    }

    private byte[] getBleAddress() {
        return (byte[]) this.getRelatedValueForLastSensorId(TableStrings.bleAddress);
    }

    private byte[] getCompositeState() {
        return (byte[]) this.getRelatedValueForLastSensorId(TableStrings.compositeState);
    }

    private int getEnableStreamingTimestamp() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.enableStreamingTimestamp);
        return l.intValue();
    }

    private boolean getEndedEarly() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.endedEarly) != 0;
    }

    private byte[] getInitialPatchInformation() {
        return (byte[]) this.getRelatedValueForLastSensorId(TableStrings.initialPatchInformation);
    }

    private int getLastScanSampleNumber() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.lastScanSampleNumber);
        return l.intValue();
    }

    private String getLastScanTimeZone() {
        return (String) this.getRelatedValueForLastSensorId(TableStrings.lastScanTimeZone);
    }

    private long getLastScanTimestampLocal() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.lastScanTimestampLocal);
    }

    private long getLastScanTimestampUTC() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.lastScanTimestampUTC);
    }

    private boolean getLsaDetected() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.lsaDetected) != 0;
    }

    private byte[] getMeasurementState() {
        return (byte[]) this.getRelatedValueForLastSensorId(TableStrings.measurementState);
    }

    private int getPersonalizationIndex() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.personalizationIndex);
        return l.intValue();
    }

    private int getSensorId() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.sensorId);
        return l.intValue();
    }

    private String getSensorStartTimeZone() {
        return (String) this.getRelatedValueForLastSensorId(TableStrings.sensorStartTimeZone);
    }

    private long getSensorStartTimestampLocal() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.sensorStartTimestampLocal);
    }

    private long getSensorStartTimestampUTC() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.getSensorStartTimestampUTC);
    }

    private String getSerialNumber() {
        return (String) this.getRelatedValueForLastSensorId(TableStrings.serialNumber);
    }

    private byte[] getStreamingAuthenticationData() {
        return (byte[]) this.getRelatedValueForLastSensorId(TableStrings.streamingAuthenticationData);
    }

    private int getStreamingUnlockCount() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.streamingUnlockCount);
        return l.intValue();
    }

    private byte[] getUniqueIdentifier() {
        return (byte[]) this.getRelatedValueForLastSensorId(TableStrings.uniqueIdentifier);
    }

    private long getUnrecordedHistoricTimeChange() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.unrecordedHistoricTimeChange);
    }

    private long getUnrecordedRealTimeTimeChange() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.unrecordedRealTimeTimeChange);
    }

    private int getUserId() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.userId);
        return l.intValue();
    }

    private int getWarmupPeriodInMinutes() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.warmupPeriodInMinutes);
        return l.intValue();
    }

    private int getWearDurationInMinutes() {
        Long l = (Long) this.getRelatedValueForLastSensorId(TableStrings.wearDurationInMinutes);
        return l.intValue();
    }

    private long computeCRC32() throws IOException {
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

    public void fillClassByValuesInLastSensorRecord() {
        this.attenuationState = getAttenuationState();
        this.bleAddress = getBleAddress();
        this.compositeState = getCompositeState();
        this.enableStreamingTimestamp = getEnableStreamingTimestamp();
        this.endedEarly = getEndedEarly();
        this.initialPatchInformation = getInitialPatchInformation();
        this.lastScanSampleNumber = getLastScanSampleNumber();
        this.lastScanTimeZone = getLastScanTimeZone();
        this.lastScanTimestampLocal = getLastScanTimestampLocal();
        this.lastScanTimestampUTC = getLastScanTimestampUTC();
        this.lsaDetected = getLsaDetected();
        this.measurementState = getMeasurementState();
        this.personalizationIndex = getPersonalizationIndex();
        this.sensorId = getSensorId();
        this.sensorStartTimeZone = getSensorStartTimeZone();
        this.sensorStartTimestampLocal = getSensorStartTimestampLocal();
        this.sensorStartTimestampUTC = getSensorStartTimestampUTC();
        this.serialNumber = getSerialNumber();
        this.streamingAuthenticationData = getStreamingAuthenticationData();
        this.streamingUnlockCount = getStreamingUnlockCount();
        this.uniqueIdentifier = getUniqueIdentifier();
        this.unrecordedHistoricTimeChange = getUnrecordedHistoricTimeChange();
        this.unrecordedRealTimeTimeChange = getUnrecordedRealTimeTimeChange();
        this.userId = getUserId();
        this.warmupPeriodInMinutes = getWarmupPeriodInMinutes();
        this.wearDurationInMinutes = getWearDurationInMinutes();
    }
}
