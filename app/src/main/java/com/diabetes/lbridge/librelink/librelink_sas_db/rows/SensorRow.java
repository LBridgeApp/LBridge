package com.diabetes.lbridge.librelink.librelink_sas_db.rows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteClosable;
import android.database.sqlite.SQLiteStatement;

import com.diabetes.lbridge.librelink.librelink_sas_db.tables.SensorTable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class SensorRow implements Row, TimeRow, ScanTimeRow, CrcRow {
    private final SensorTable table;
    private final List<SQLiteStatement> sqlChangingList = new ArrayList<>();

    public SensorRow(final SensorTable table,
                     final int rowIndex) {
        this.table = table;

        String query = Row.getBaseRowSearchingSQL(table);
        Cursor cursor = table.getDatabase().getSQLite().rawQuery(query, null);
        cursor.moveToPosition(rowIndex); // перемещаемся на строку с индексом rowIndex

        this.attenuationState = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.attenuationState));
        this.bleAddress = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.bleAddress));
        this.compositeState = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.compositeState));
        this.enableStreamingTimestamp = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.enableStreamingTimestamp));
        this.endedEarly = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.endedEarly)) != 0;
        this.initialPatchInformation = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.initialPatchInformation));
        this.lastScanSampleNumber = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.lastScanSampleNumber));
        this.lastScanTimeZone = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.lastScanTimeZone));
        this.lastScanTimestampLocal = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.lastScanTimestampLocal));
        this.lastScanTimestampUTC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.lastScanTimestampUTC));
        this.lsaDetected = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.lsaDetected)) != 0;
        this.measurementState = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.measurementState));
        this.personalizationIndex = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.personalizationIndex));
        this.sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.sensorId));
        this.sensorStartTimeZone = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.sensorStartTimeZone));
        this.sensorStartTimestampLocal = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.sensorStartTimestampLocal));
        this.sensorStartTimestampUTC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.sensorStartTimestampUTC));
        this.serialNumber = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.serialNumber));
        this.streamingAuthenticationData = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.streamingAuthenticationData));
        this.streamingUnlockCount = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.streamingUnlockCount));
        this.uniqueIdentifier = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.uniqueIdentifier));
        this.unrecordedHistoricTimeChange = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.unrecordedHistoricTimeChange));
        this.unrecordedRealTimeTimeChange = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.unrecordedRealTimeTimeChange));
        this.userId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.userId));
        this.warmupPeriodInMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.warmupPeriodInMinutes));
        this.wearDurationInMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.wearDurationInMinutes));
        this.CRC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.CRC));

        cursor.close();
    }

    public SensorRow(final SensorTable table,
                     final byte[] attenuationState,
                     final byte[] bleAddress,
                     final byte[] compositeState,
                     final int enableStreamingTimestamp,
                     final boolean endedEarly,
                     final byte[] initialPatchInformation,
                     final int lastScanSampleNumber,
                     final String lastScanTimeZone,
                     final long lastScanTimestampLocal,
                     final long lastScanTimestampUTC,
                     final boolean lsaDetected,
                     final byte[] measurementState,
                     final int personalizationIndex,
                     final String sensorStartTimeZone,
                     final long sensorStartTimestampLocal,
                     final long sensorStartTimestampUTC,
                     final String serialNumber,
                     final byte[] streamingAuthenticationData,
                     final int streamingUnlockCount,
                     final byte[] uniqueIdentifier,
                     final long unrecordedHistoricTimeChange,
                     final long unrecordedRealTimeTimeChange,
                     final int userId,
                     final int warmupPeriodInMinutes,
                     final int wearDurationInMinutes) {
        this.table = table;

        this.attenuationState = attenuationState;
        this.bleAddress = bleAddress;
        this.compositeState = compositeState;
        this.enableStreamingTimestamp = enableStreamingTimestamp;
        this.endedEarly = endedEarly;
        this.initialPatchInformation = initialPatchInformation;
        this.lastScanSampleNumber = lastScanSampleNumber;
        this.lastScanTimeZone = lastScanTimeZone;
        this.lastScanTimestampLocal = lastScanTimestampLocal;
        this.lastScanTimestampUTC = lastScanTimestampUTC;
        this.lsaDetected = lsaDetected;
        this.measurementState = measurementState;
        this.personalizationIndex = personalizationIndex;
        this.sensorId = table.getLastSensorId() + 1;
        this.sensorStartTimeZone = sensorStartTimeZone;
        this.sensorStartTimestampLocal = sensorStartTimestampLocal;
        this.sensorStartTimestampUTC = sensorStartTimestampUTC;
        this.serialNumber = serialNumber;
        this.streamingAuthenticationData = streamingAuthenticationData;
        this.streamingUnlockCount = streamingUnlockCount;
        this.uniqueIdentifier = uniqueIdentifier;
        this.unrecordedHistoricTimeChange = unrecordedHistoricTimeChange;
        this.unrecordedRealTimeTimeChange = unrecordedRealTimeTimeChange;
        this.userId = userId;
        this.warmupPeriodInMinutes = warmupPeriodInMinutes;
        this.wearDurationInMinutes = wearDurationInMinutes;
    }

    @Override
    public void insertOrThrow() throws Exception {

        ContentValues values = new ContentValues();
        values.put(RowColumns.attenuationState, attenuationState);
        values.put(RowColumns.bleAddress, bleAddress);
        values.put(RowColumns.compositeState, compositeState);
        values.put(RowColumns.enableStreamingTimestamp, enableStreamingTimestamp);
        values.put(RowColumns.endedEarly, endedEarly);
        values.put(RowColumns.initialPatchInformation, initialPatchInformation);
        values.put(RowColumns.lastScanSampleNumber, lastScanSampleNumber);
        values.put(RowColumns.lastScanTimeZone, lastScanTimeZone);
        values.put(RowColumns.lastScanTimestampLocal, lastScanTimestampLocal);
        values.put(RowColumns.lastScanTimestampUTC, lastScanTimestampUTC);
        values.put(RowColumns.lsaDetected, lsaDetected);
        values.put(RowColumns.measurementState, measurementState);
        values.put(RowColumns.personalizationIndex, personalizationIndex);
        // не нужно писать sensorId, так как это значение само увеличивается при добавлении записи.
        values.put(RowColumns.sensorStartTimeZone, sensorStartTimeZone);
        values.put(RowColumns.sensorStartTimestampLocal, sensorStartTimestampLocal);
        values.put(RowColumns.sensorStartTimestampUTC, sensorStartTimestampUTC);
        values.put(RowColumns.serialNumber, serialNumber);
        values.put(RowColumns.streamingAuthenticationData, streamingAuthenticationData);
        values.put(RowColumns.streamingUnlockCount, streamingUnlockCount);
        values.put(RowColumns.uniqueIdentifier, uniqueIdentifier);
        values.put(RowColumns.unrecordedHistoricTimeChange, unrecordedHistoricTimeChange);
        values.put(RowColumns.unrecordedRealTimeTimeChange, unrecordedRealTimeTimeChange);
        values.put(RowColumns.userId, userId);
        values.put(RowColumns.warmupPeriodInMinutes, warmupPeriodInMinutes);
        values.put(RowColumns.wearDurationInMinutes, wearDurationInMinutes);
        values.put(RowColumns.CRC, this.computeCRC32());

        table.getDatabase().getSQLite().insertOrThrow(table.getName(), null, values);
        table.rowInserted();
    }

    public void replace() throws Exception {
        this.setCRC(this.computeCRC32()); // setCRC добавляет SQL запрос на изменение CRC
        sqlChangingList.forEach(SQLiteStatement::execute);
        sqlChangingList.forEach(SQLiteClosable::close);
        sqlChangingList.clear();
    }

    public byte[] getAttenuationState() {
        return attenuationState;
    }

    public byte[] getCompositeState() {
        return compositeState;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getSensorId() {
        return sensorId;
    }

    public long getSensorStartTimestampUTC() {
        return sensorStartTimestampUTC;
    }

    public boolean getEndedEarly() {
        return endedEarly;
    }

    public SensorRow setAttenuationState(byte[] attenuationState) {
        this.attenuationState = attenuationState;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(table, RowColumns.attenuationState, RowColumns.sensorId, sensorId));
            if (attenuationState == null) {
                statement.bindNull(1);
            } else {
                statement.bindBlob(1, attenuationState);
            }
            sqlChangingList.add(statement);
        return this;
    }


    public SensorRow setCompositeState(byte[] compositeState) {
        this.compositeState = compositeState;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL
                        (table, RowColumns.compositeState, RowColumns.sensorId, sensorId));
        if (compositeState == null) {
            statement.bindNull(1);
        } else {
            statement.bindBlob(1, compositeState);
        }
        sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setLastScanSampleNumber(int lastScanSampleNumber) {
        this.lastScanSampleNumber = lastScanSampleNumber;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.lastScanSampleNumber, RowColumns.sensorId, sensorId));
        statement.bindLong(1, lastScanSampleNumber);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setLastScanTimeZone(String lastScanTimeZone) {
        this.lastScanTimeZone = lastScanTimeZone;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(table, RowColumns.lastScanTimeZone, RowColumns.sensorId, sensorId));
        statement.bindString(1, lastScanTimeZone);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setLastScanTimestampLocal(long lastScanTimestampLocal) {
        this.lastScanTimestampLocal = lastScanTimestampLocal;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.lastScanTimestampLocal, RowColumns.sensorId, sensorId));
        statement.bindLong(1, lastScanTimestampLocal);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setLastScanTimestampUTC(long lastScanTimestampUTC) {
        this.lastScanTimestampUTC = lastScanTimestampUTC;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(table, RowColumns.lastScanTimestampUTC, RowColumns.sensorId, sensorId));
        statement.bindLong(1, lastScanTimestampUTC);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.serialNumber, RowColumns.sensorId, sensorId));
        statement.bindString(1, serialNumber);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setUniqueIdentifier(byte[] uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.uniqueIdentifier, RowColumns.sensorId, sensorId));
        statement.bindBlob(1, uniqueIdentifier);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setSensorStartTimestampLocal(long sensorStartTimestampLocal) {
        this.sensorStartTimestampLocal = sensorStartTimestampLocal;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.sensorStartTimestampLocal, RowColumns.sensorId, sensorId));
        statement.bindLong(1, sensorStartTimestampLocal);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setSensorStartTimestampUTC(long sensorStartTimestampUTC) {
        this.sensorStartTimestampUTC = sensorStartTimestampUTC;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.sensorStartTimestampUTC, RowColumns.sensorId, sensorId));
        statement.bindLong(1, sensorStartTimestampUTC);
        this.sqlChangingList.add(statement);
        return this;
    }

    private void setCRC(long CRC) {
        this.CRC = CRC;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.CRC, RowColumns.sensorId, sensorId));
        statement.bindLong(1, CRC);
        this.sqlChangingList.add(statement);
    }

    public SensorRow setEndedEarly(boolean endedEarly) {
        this.endedEarly = endedEarly;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.endedEarly, RowColumns.sensorId, sensorId));
        statement.bindLong(1, (endedEarly) ? 1 : 0);
        this.sqlChangingList.add(statement);
        return this;
    }

    public SensorRow setWearDurationInMinutes(int wearDurationInMinutes){
        this.wearDurationInMinutes = wearDurationInMinutes;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(
                        table, RowColumns.wearDurationInMinutes,
                        RowColumns.sensorId, sensorId));
        statement.bindLong(1, wearDurationInMinutes);
        this.sqlChangingList.add(statement);
        return this;
    }

    public long computeCRC32() throws Exception {
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

    private byte[] attenuationState;
    private final byte[] bleAddress;
    private byte[] compositeState;
    private final int enableStreamingTimestamp;
    private boolean endedEarly;
    private final byte[] initialPatchInformation;
    private int lastScanSampleNumber;
    private String lastScanTimeZone;
    private long lastScanTimestampLocal;
    private long lastScanTimestampUTC;
    private final boolean lsaDetected;
    private final byte[] measurementState;
    private final int personalizationIndex;
    private final int sensorId;
    private final String sensorStartTimeZone;
    private long sensorStartTimestampLocal;
    private long sensorStartTimestampUTC;
    private String serialNumber;
    private final byte[] streamingAuthenticationData;
    private final int streamingUnlockCount;
    private byte[] uniqueIdentifier;
    private final long unrecordedHistoricTimeChange;
    private final long unrecordedRealTimeTimeChange;
    private final int userId;
    private final int warmupPeriodInMinutes;
    private int wearDurationInMinutes;
    private long CRC;

    @Override
    public long getBiggestTimestampUTC() {
        return Math.max(this.lastScanTimestampUTC, sensorStartTimestampUTC);
    }

    @Override
    public long getScanTimestampUTC() {
        return this.lastScanTimestampUTC;
    }
    @Override
    public void validateCRC() throws Exception {
        if(this.CRC != this.computeCRC32()){
            throw new Exception("CRC is not valid.");
        }
    }

    private static class RowColumns {
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
