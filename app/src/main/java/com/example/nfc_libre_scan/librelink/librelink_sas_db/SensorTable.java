package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteStatement;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.libre.PatchUID;
import com.oop1.CurrentBg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.zip.CRC32;

public class SensorTable implements CrcTable, TimeTable {

    private final LibreLinkDatabase db;
    private final LibreMessage libreMessage;
    private final UserTable userTable;
    private final SensorSelectionRangeTable sensorSelectionRangeTable;

    public SensorTable(LibreLinkDatabase db) throws Exception {
        this.db = db;
        this.libreMessage = db.getLibreMessage();
        this.userTable = db.getUserTable();
        this.sensorSelectionRangeTable = db.getSensorSelectionRangeTable();

        this.onTableClassInit();
    }

    private static boolean isSensorExpired(long sensorStartTimestampUTC) {
        Instant startInstant = Instant.ofEpochMilli(sensorStartTimestampUTC);
        Instant endInstant = startInstant.plus(14, ChronoUnit.DAYS);
        long sensorExpirationTimestamp = endInstant.toEpochMilli();

        long currentTimestamp = System.currentTimeMillis();

        return currentTimestamp >= sensorExpirationTimestamp;
    }

    private boolean isSensorExtended(){
        String originalLibreSN = libreMessage.getLibreSN();

        boolean sensorIsExtended;
        String sql = String.format("SELECT %s FROM %s WHERE %s=%s",
                TableStrings.sensorStartTimestampUTC, TableStrings.TABLE_NAME,
                TableStrings.serialNumber, originalLibreSN);

        Cursor cursor = db.getObject().rawQuery(sql, null);
        if(cursor.moveToFirst()){
            long startTimestampOfSensorWithOriginalSerialNumber = cursor.getLong(0);
            sensorIsExtended = isSensorExpired(startTimestampOfSensorWithOriginalSerialNumber);
        }
        else{
            sensorIsExtended = false;
        }
        cursor.close();

        return sensorIsExtended;
    }


    protected Integer getLastStoredSensorId() {
        return SqlUtils.getLastStoredFieldValue(db.getObject(), TableStrings.sensorId, TableStrings.TABLE_NAME);
    }

    private Object getRelatedValueForLastSensorId(String fieldName) {
        final int lastStoredSensorId = getLastStoredSensorId();
        return SqlUtils.getRelatedValue(db.getObject(), fieldName, TableStrings.TABLE_NAME, TableStrings.sensorId, lastStoredSensorId);
    }

    private void createNewSensorRecord() throws Exception {
        if(isSensorExtended()){
            // если оригинальный серийный номер уже присутствует в базе, а сенсор продленный.
            this.uniqueIdentifier = PatchUID.generateFake();
            this.serialNumber = PatchUID.decodeSerialNumber(this.uniqueIdentifier);
        }
        else {
            // если сенсор новый или всё-таки продленный, но отсутствует в базе.
            this.uniqueIdentifier = libreMessage.getRawLibreData().getPatchUID();
            this.serialNumber = libreMessage.getLibreSN();
        }

        this.attenuationState = libreMessage.getLibreSavedState().getAttenuationState();
        this.bleAddress = null;
        this.compositeState = libreMessage.getLibreSavedState().getCompositeState();
        this.enableStreamingTimestamp = 0;
        this.endedEarly = false;
        this.initialPatchInformation = libreMessage.getRawLibreData().getPatchInfo();
        this.lastScanSampleNumber = libreMessage.getCurrentBg().getSampleNumber();
        this.lastScanTimeZone = libreMessage.getCurrentBg().getTimeZone();
        this.lastScanTimestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        this.lastScanTimestampUTC = libreMessage.getCurrentBg().getTimestampUTC();
        this.lsaDetected = false;
        this.measurementState = null;
        this.personalizationIndex = 0;
        // не нужно писать sensorId, так как это значение само увеличивается при добавлении записи.
        this.sensorStartTimeZone = libreMessage.getCurrentBg().getTimeZone();
        this.sensorStartTimestampLocal = libreMessage.getSensorStartTimestampLocal();
        this.sensorStartTimestampUTC = libreMessage.getSensorStartTimestampUTC();
        this.streamingAuthenticationData = null;
        this.streamingUnlockCount = 0;
        this.unrecordedHistoricTimeChange = 0;
        this.unrecordedRealTimeTimeChange = 0;
        this.userId = userTable.getLastStoredUserId();
        this.warmupPeriodInMinutes = 60;
        this.wearDurationInMinutes = 20160;
        this.CRC = this.computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.attenuationState, attenuationState);
        values.put(TableStrings.bleAddress, bleAddress);
        values.put(TableStrings.compositeState, compositeState);
        values.put(TableStrings.enableStreamingTimestamp, enableStreamingTimestamp);
        values.put(TableStrings.endedEarly, endedEarly);
        values.put(TableStrings.initialPatchInformation, initialPatchInformation);
        values.put(TableStrings.lastScanSampleNumber, lastScanSampleNumber);
        values.put(TableStrings.lastScanTimeZone, lastScanTimeZone);
        values.put(TableStrings.lastScanTimestampLocal, lastScanTimestampLocal);
        values.put(TableStrings.lastScanTimestampUTC, lastScanTimestampUTC);
        values.put(TableStrings.lsaDetected, lsaDetected);
        values.put(TableStrings.measurementState, measurementState);
        values.put(TableStrings.personalizationIndex, personalizationIndex);
        // не нужно писать sensorId, так как это значение само увеличивается при добавлении записи.
        values.put(TableStrings.sensorStartTimeZone, sensorStartTimeZone);
        values.put(TableStrings.sensorStartTimestampLocal, sensorStartTimestampLocal);
        values.put(TableStrings.sensorStartTimestampUTC, sensorStartTimestampUTC);
        values.put(TableStrings.serialNumber, serialNumber);
        values.put(TableStrings.streamingAuthenticationData, streamingAuthenticationData);
        values.put(TableStrings.streamingUnlockCount, streamingUnlockCount);
        values.put(TableStrings.uniqueIdentifier, uniqueIdentifier);
        values.put(TableStrings.unrecordedHistoricTimeChange, unrecordedHistoricTimeChange);
        values.put(TableStrings.unrecordedRealTimeTimeChange, unrecordedRealTimeTimeChange);
        values.put(TableStrings.userId, userId);
        values.put(TableStrings.warmupPeriodInMinutes, warmupPeriodInMinutes);
        values.put(TableStrings.wearDurationInMinutes, wearDurationInMinutes);
        values.put(TableStrings.CRC, CRC);

        db.getObject().insertOrThrow(TableStrings.TABLE_NAME, null, values);
        this.onTableChanged();
    }

    private void updateLastSensorRecord() throws Exception {
        final byte[] messageAttenuationState = libreMessage.getLibreSavedState().getAttenuationState();
        final byte[] messageCompositeState = libreMessage.getLibreSavedState().getCompositeState();

        final byte[] tableAttenuationState = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.attenuationState);
        final byte[] tableCompositeState = (byte[]) this.getRelatedValueForLastSensorId(TableStrings.compositeState);

        // Если в таблице LibreLink attenuationState и compositeState
        // не равны null, а в libreMessage равны null, то не перезаписывать.
        this.attenuationState = (messageAttenuationState != null) ? messageAttenuationState : tableAttenuationState;
        this.compositeState = (messageCompositeState != null) ? messageCompositeState : tableCompositeState;

        this.sensorId = getLastStoredSensorId();
        this.lastScanSampleNumber = libreMessage.getCurrentBg().getSampleNumber();
        this.lastScanTimeZone = libreMessage.getCurrentBg().getTimeZone();
        this.lastScanTimestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        this.lastScanTimestampUTC = libreMessage.getCurrentBg().getTimestampUTC();
        this.CRC = this.computeCRC32();

        String sql = String.format("UPDATE %s SET %s=%s, %s=%s, %s=%s, %s=%s, %s=?, %s=?, %s=%s WHERE %s=%s;",
                TableStrings.TABLE_NAME,
                TableStrings.lastScanSampleNumber, lastScanSampleNumber,
                TableStrings.lastScanTimeZone, DatabaseUtils.sqlEscapeString(lastScanTimeZone),
                TableStrings.lastScanTimestampLocal, lastScanTimestampLocal,
                TableStrings.lastScanTimestampUTC, lastScanTimestampUTC,
                TableStrings.attenuationState,
                TableStrings.compositeState,
                TableStrings.CRC, CRC,
                TableStrings.sensorId, this.sensorId);

        try (SQLiteStatement statement = db.getObject().compileStatement(sql)) {

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
        }

        this.onTableChanged();
    }

    public void updateToLastScan() throws Exception {
        SqlUtils.validateTime(this, libreMessage);

        if(isTableNull() || isSensorExpired(libreMessage.getSensorStartTimestampUTC())){
            this.createNewSensorRecord();
            sensorSelectionRangeTable.createNewSensorRecord();
        }
        else {
            this.updateLastSensorRecord();
        }
    }
    @Override
    public void onTableChanged() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.WRITING);
    }

    @Override
    public void onTableClassInit() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.READING);
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
        this.sensorStartTimestampUTC = (long) this.getRelatedValueForLastSensorId(TableStrings.sensorStartTimestampUTC);
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
    public long getLastUTCTimestamp() {
        return (long) this.getRelatedValueForLastSensorId(TableStrings.lastScanTimestampUTC);
    }

    @Override
    public boolean isTableNull() {
        return SqlUtils.isTableNull(this.db.getObject(), TableStrings.TABLE_NAME);
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
        final static String sensorStartTimestampUTC = "sensorStartTimestampUTC";
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
