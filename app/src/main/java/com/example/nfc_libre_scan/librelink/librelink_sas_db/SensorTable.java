package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteStatement;

import com.example.nfc_libre_scan.Logger;
import com.example.nfc_libre_scan.Utils;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.libre.PatchUID;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.zip.CRC32;

public class SensorTable implements CrcTable, TimeTable {

    private final LibreLinkDatabase db;
    private final LibreMessage libreMessage;

    public SensorTable(LibreLinkDatabase db) throws Exception {
        this.db = db;
        this.libreMessage = db.getLibreMessage();
    }

    private boolean isLastSensorExpired(){
        // нужно учитывать, что уникальный идентификатор сенсора может быть фейковым.
        String lastSensorSerialNumber = (String) this.getRelatedValueForLastSensorId(TableStrings.serialNumber);
        return isSensorExpired(lastSensorSerialNumber);
    }

    private boolean isSensorExpired(String libreSN) {
        String sql = String.format("SELECT * FROM %s WHERE %s=%s", TableStrings.TABLE_NAME,
                TableStrings.serialNumber, DatabaseUtils.sqlEscapeString(libreSN));

        Cursor cursor = db.getSQLite().rawQuery(sql, null);

        boolean sensorIsExpired;
        if(cursor.moveToFirst()){
            int sensorStartTimestampIndex = cursor.getColumnIndex(TableStrings.sensorStartTimestampUTC);
            int endedEarlyIndex = cursor.getColumnIndex(TableStrings.endedEarly);

            long sensorStartTimestampUTC = cursor.getLong(sensorStartTimestampIndex);
            boolean endedEarly = cursor.getInt(endedEarlyIndex) != 0;

            Instant startInstant = Instant.ofEpochMilli(sensorStartTimestampUTC);
            Instant endInstant = startInstant.plus(14, ChronoUnit.DAYS);
            long sensorExpirationTimestamp = endInstant.toEpochMilli();

            long currentTimestamp = System.currentTimeMillis();

            sensorIsExpired = currentTimestamp >= sensorExpirationTimestamp || endedEarly;
        }
        else{ sensorIsExpired = false; }
        cursor.close();
        return sensorIsExpired;
    }

    private boolean isSensorExists(String libreSN){
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s=%s", TableStrings.TABLE_NAME,
                TableStrings.serialNumber, DatabaseUtils.sqlEscapeString(libreSN));

        Cursor cursor = db.getSQLite().rawQuery(sql, null);

        int count = 0;
        if(cursor.moveToFirst()){
            count = cursor.getInt(0);
        }
        cursor.close();
        return count != 0;
    }

    protected Integer getLastStoredSensorId() {
        return SqlUtils.getLastStoredFieldValue(db.getSQLite(), TableStrings.sensorId, TableStrings.TABLE_NAME);
    }

    private Object getRelatedValueForLastSensorId(String fieldName) {
        final int lastStoredSensorId = getLastStoredSensorId();
        return SqlUtils.getRelatedValue(db.getSQLite(), fieldName, TableStrings.TABLE_NAME, TableStrings.sensorId, lastStoredSensorId);
    }

    private void createNewSensorRecord(boolean sensorIsExtended) throws Exception {
        if (sensorIsExtended) {
            // если оригинальный серийный номер уже присутствует в базе, а сенсор продленный.
            this.uniqueIdentifier = PatchUID.generateFake();
            this.serialNumber = PatchUID.decodeSerialNumber(this.uniqueIdentifier);
        } else {
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
        this.sensorStartTimestampLocal = Utils.withoutNanos(libreMessage.getSensorStartTimestampLocal());
        this.sensorStartTimestampUTC = Utils.withoutNanos(libreMessage.getSensorStartTimestampUTC());
        this.streamingAuthenticationData = null;
        this.streamingUnlockCount = 0;
        this.unrecordedHistoricTimeChange = 0;
        this.unrecordedRealTimeTimeChange = 0;
        this.userId = db.getUserTable().getLastStoredUserId();
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

        db.getSQLite().insertOrThrow(TableStrings.TABLE_NAME, null, values);
        this.onNewSensorRecord();
        this.onTableChanged();
    }

    private void onNewSensorRecord() throws Exception {
        db.getSensorSelectionRangeTable().patchWithNewSensor();
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

        String sql = String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=?",
                TableStrings.TABLE_NAME,
                TableStrings.attenuationState,
                TableStrings.compositeState,
                TableStrings.lastScanSampleNumber,
                TableStrings.lastScanTimeZone,
                TableStrings.lastScanTimestampLocal,
                TableStrings.lastScanTimestampUTC,
                TableStrings.CRC,
                TableStrings.sensorId
                );

        try (SQLiteStatement statement = db.getSQLite().compileStatement(sql)) {
            if(attenuationState != null){
                statement.bindBlob(1, attenuationState);
            }
            else{
                statement.bindNull(1);
            }

            if(compositeState != null){
                statement.bindBlob(2, compositeState);
            }
            else {
                statement.bindNull(2);
            }

            statement.bindLong(3, lastScanSampleNumber);
            statement.bindString(4, lastScanTimeZone);
            statement.bindLong(5, lastScanTimestampLocal);
            statement.bindLong(6, lastScanTimestampUTC);
            statement.bindLong(7, CRC);
            statement.bindLong(8, sensorId);

            statement.execute();
        }

        this.onTableChanged();
    }

    public void updateToLastScan() throws Exception {
        SqlUtils.validateTime(this, libreMessage);
        boolean isSensorExists = isSensorExists(libreMessage.getLibreSN());
        boolean isLastSensorExpired = isLastSensorExpired();

        if(!isSensorExists){
            String originalLibreSN = libreMessage.getLibreSN();
            Logger.inf(String.format("Sensor with serial number %s does not exists in db. " +
                    "Creating new sensor record...", originalLibreSN));
            // если сенсор новый или всё-таки продленный, но отсутствует в базе.
            this.createNewSensorRecord(false);
        }
        else if(isLastSensorExpired){
            // если срок действия последнего сенсора истёк
            String tableLibreSN = (String) this.getRelatedValueForLastSensorId(TableStrings.serialNumber);
            Logger.inf(String.format("Sensor with serial number %s expired in db. " +
                    "Creating new sensor record with fake ID...", tableLibreSN));
            this.createNewSensorRecord(true);
        }
        else {
            Logger.inf("Updating last sensor record...");
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
        return SqlUtils.isTableNull(this.db.getSQLite(), TableStrings.TABLE_NAME);
    }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        String sql = String.format("UPDATE %s SET %s=?, %s=?, %s=? WHERE %s=?", TableStrings.TABLE_NAME,
                TableStrings.uniqueIdentifier,
                TableStrings.serialNumber,
                TableStrings.CRC,
                TableStrings.sensorId);

        this.uniqueIdentifier = PatchUID.generateFake();
        this.serialNumber = PatchUID.decodeSerialNumber(uniqueIdentifier);
        this.sensorId = getLastStoredSensorId();
        this.CRC = computeCRC32();
        try (SQLiteStatement statement = db.getSQLite().compileStatement(sql)) {
            statement.bindBlob(1, uniqueIdentifier);
            statement.bindString(2, serialNumber);
            statement.bindLong(3, CRC);
            statement.bindLong(4, sensorId);
            statement.execute();
        }

        this.onTableChanged();
    }

    public void endCurrentSensor() throws Exception {
        String sql = String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=? WHERE %s=?",
                TableStrings.TABLE_NAME,
                TableStrings.sensorStartTimestampLocal,
                TableStrings.sensorStartTimestampUTC,
                TableStrings.endedEarly,
                TableStrings.CRC,
                TableStrings.sensorId);

        this.sensorStartTimestampUTC = Utils.withoutNanos(LocalDateTime.now().minusDays(14).toInstant(ZoneOffset.UTC).toEpochMilli());
        this.sensorStartTimestampLocal = Utils.withoutNanos(Utils.unixAsLocal(sensorStartTimestampUTC));
        this.endedEarly = false;
        this.sensorId = getLastStoredSensorId();
        this.CRC = computeCRC32();

        try (SQLiteStatement statement = db.getSQLite().compileStatement(sql)) {
            statement.bindLong(1, this.sensorStartTimestampLocal);
            statement.bindLong(2, this.sensorStartTimestampUTC);
            statement.bindLong(3, (this.endedEarly) ? 1 : 0);
            statement.bindLong(4, CRC);
            statement.bindLong(5, sensorId);

            statement.execute();
        }

        this.onSensorEnded();
        this.onTableChanged();
    }

    private void onSensorEnded() {
        // путем опытов пришел к выводу,
        // что здесь не нужно заканчивать сенсор
        // в таблице SensorSelectionRanges.
        // На самом деле, так лучше,
        // потому что в приложении будет надпись,
        // что работа сенсора закончена.
        // Если в SensorSelectionRanges сенсор закончить,
        // то красная надпись пропадет, а будет просто желтая полоса
        // с предложением отсканировать новый сенсор.
        //db.getSensorSelectionRangeTable().endCurrentSensor();
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
