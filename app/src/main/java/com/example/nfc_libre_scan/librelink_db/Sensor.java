package com.example.nfc_libre_scan.librelink_db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class Sensor {

    private final SQLiteDatabase db;
    private int lastSensorIdForSearch = 0;

    private int getLastSensorIdForSearch() {
        if (lastSensorIdForSearch == 0) {
            this.lastSensorIdForSearch = this.getLastSensorId();
        }
        return lastSensorIdForSearch;
    }

    private int getLastSensorId() {
        final String sql = "SELECT sensorId FROM sensors ORDER BY sensorId ASC LIMIT 1 OFFSET (SELECT COUNT(*) FROM sensors)-1;";
        Cursor cursor = db.rawQuery(sql, null);
        int sensorId = 1;
        if (cursor.moveToFirst()) {
            sensorId = cursor.getInt(0);
        }
        cursor.close();
        return sensorId;
    }

    private Object getCellValueFromDB(String fieldName) {
        final int lastSensorId = getLastSensorIdForSearch();
        String sql = String.format("SELECT %s FROM sensors WHERE sensorId='%s'", fieldName, lastSensorId);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        Object data = null;
        if (cursor.getType(0) == Cursor.FIELD_TYPE_INTEGER) {
            data = cursor.getLong(0);
        } else if (cursor.getType(0) == Cursor.FIELD_TYPE_FLOAT) {
            data = cursor.getFloat(0);
        } else if (cursor.getType(0) == Cursor.FIELD_TYPE_STRING) {
            data = cursor.getString(0);
        } else if (cursor.getType(0) == Cursor.FIELD_TYPE_BLOB) {
            data = cursor.getBlob(0);
        }
        cursor.close();
        return data;
    }

    public long getComputedCRC(){ return this.computedCRC; }

    public Sensor(SQLiteDatabase db) throws IOException {
        this.db = db;

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
        this.computedCRC = computeCRC32();
    }

    private final byte[] attenuationState;
    private final byte[] bleAddress;
    private final byte[] compositeState;
    private final int enableStreamingTimestamp;
    private final boolean endedEarly;
    private final byte[] initialPatchInformation;
    private final int lastScanSampleNumber;
    private final String lastScanTimeZone;
    private final long lastScanTimestampLocal;
    private final long lastScanTimestampUTC;
    private final boolean lsaDetected;
    private final byte[] measurementState;
    private final int personalizationIndex;
    private final int sensorId;
    private final String sensorStartTimeZone;
    private final long sensorStartTimestampLocal;
    private final long sensorStartTimestampUTC;
    private final String serialNumber;
    private final byte[] streamingAuthenticationData;
    private final int streamingUnlockCount;
    private final byte[] uniqueIdentifier;
    private final long unrecordedHistoricTimeChange;
    private final long unrecordedRealTimeTimeChange;
    private final int userId;
    private final int warmupPeriodInMinutes;
    private final int wearDurationInMinutes;
    private final long computedCRC;

    private byte[] getAttenuationState() {
        return (byte[]) this.getCellValueFromDB(TableFields.attenuationState);
    }

    private byte[] getBleAddress() {
        return (byte[]) this.getCellValueFromDB(TableFields.bleAddress);
    }

    private byte[] getCompositeState() {
        return (byte[]) this.getCellValueFromDB(TableFields.compositeState);
    }

    private int getEnableStreamingTimestamp() {
        Long l = (Long) this.getCellValueFromDB(TableFields.enableStreamingTimestamp);
        return l.intValue();
    }

    private boolean getEndedEarly() {
        return (long) this.getCellValueFromDB(TableFields.endedEarly) != 0;
    }

    private byte[] getInitialPatchInformation() {
        return (byte[]) this.getCellValueFromDB(TableFields.initialPatchInformation);
    }

    private int getLastScanSampleNumber() {
        Long l = (Long) this.getCellValueFromDB(TableFields.lastScanSampleNumber);
        return l.intValue();
    }

    private String getLastScanTimeZone() {
        return (String) this.getCellValueFromDB(TableFields.lastScanTimeZone);
    }

    private long getLastScanTimestampLocal() {
        return (long) this.getCellValueFromDB(TableFields.lastScanTimestampLocal);
    }

    private long getLastScanTimestampUTC() {
        return (long) this.getCellValueFromDB(TableFields.lastScanTimestampUTC);
    }

    private boolean getLsaDetected() {
        return (long) this.getCellValueFromDB(TableFields.lsaDetected) != 0;
    }

    private byte[] getMeasurementState() {
        return (byte[]) this.getCellValueFromDB(TableFields.measurementState);
    }

    private int getPersonalizationIndex() {
        Long l = (Long) this.getCellValueFromDB(TableFields.personalizationIndex);
        return l.intValue();
    }

    private int getSensorId() {
        Long l = (Long) this.getCellValueFromDB(TableFields.sensorId);
        return l.intValue();
    }

    private String getSensorStartTimeZone() {
        return (String) this.getCellValueFromDB(TableFields.sensorStartTimeZone);
    }

    private long getSensorStartTimestampLocal() {
        return (long) this.getCellValueFromDB(TableFields.sensorStartTimestampLocal);
    }

    private long getSensorStartTimestampUTC() {
        return (long) this.getCellValueFromDB(TableFields.getSensorStartTimestampUTC);
    }

    private String getSerialNumber() {
        return (String) this.getCellValueFromDB(TableFields.serialNumber);
    }

    private byte[] getStreamingAuthenticationData() {
        return (byte[]) this.getCellValueFromDB(TableFields.streamingAuthenticationData);
    }

    private int getStreamingUnlockCount() {
        Long l = (Long) this.getCellValueFromDB(TableFields.streamingUnlockCount);
        return l.intValue();
    }

    private byte[] getUniqueIdentifier() {
        return (byte[]) this.getCellValueFromDB(TableFields.uniqueIdentifier);
    }

    private long getUnrecordedHistoricTimeChange() {
        return (long) this.getCellValueFromDB(TableFields.unrecordedHistoricTimeChange);
    }

    private long getUnrecordedRealTimeTimeChange() {
        return (long) this.getCellValueFromDB(TableFields.unrecordedRealTimeTimeChange);
    }

    private int getUserId() {
        Long l = (Long) this.getCellValueFromDB(TableFields.userId);
        return l.intValue();
    }

    private int getWarmupPeriodInMinutes() {
        Long l = (Long) this.getCellValueFromDB(TableFields.warmupPeriodInMinutes);
        return l.intValue();
    }

    private int getWearDurationInMinutes() {
        Long l = (Long) this.getCellValueFromDB(TableFields.wearDurationInMinutes);
        return l.intValue();
    }

    private static class TableFields {
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
}
