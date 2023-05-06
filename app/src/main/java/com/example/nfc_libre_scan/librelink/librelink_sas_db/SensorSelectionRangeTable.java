package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteStatement;

import com.example.nfc_libre_scan.libre.LibreMessage;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class SensorSelectionRangeTable implements CrcTable, TimeTable {

    private final LibreLinkDatabase db;
    private final LibreMessage libreMessage;

    SensorSelectionRangeTable(LibreLinkDatabase db) {
        this.db = db;
        this.libreMessage = db.getLibreMessage();
    }
    @Override
    public void onTableClassInit() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.READING);
    }

    @Override
    public void fillByLastRecord() {
        this.endTimestampUTC = (long) this.getRelatedValueForLastRangeId(TableStrings.endTimestampUTC);
        this.rangeId = ((Long)this.getRelatedValueForLastRangeId(TableStrings.rangeId)).intValue();
        this.sensorId = ((Long) this.getRelatedValueForLastRangeId(TableStrings.sensorId)).intValue();
        this.startTimestampUTC = (long) this.getRelatedValueForLastRangeId(TableStrings.startTimestampUTC);
        this.CRC = (long) this.getRelatedValueForLastRangeId(TableStrings.CRC);
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
        dataOutputStream.writeLong(this.startTimestampUTC);
        dataOutputStream.writeLong(this.endTimestampUTC);

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
        return (long) this.getRelatedValueForLastRangeId(TableStrings.startTimestampUTC);
    }

    @Override
    public boolean isTableNull() {
        return SqlUtils.isTableNull(this.db.getSQLite(), TableStrings.TABLE_NAME);
    }

    @Override
    public void onTableChanged() throws Exception {
        this.fillByLastRecord();

        if(this.sensorId != this.rangeId){
            throw new Exception("RangeId is not equals sensorId");
        }

        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.WRITING);
    }

    private Object getRelatedValueForLastRangeId(String fieldName) {
        final Integer lastStoredRangeId = this.getLastStoredRangeId();
        return SqlUtils.getRelatedValue(db.getSQLite(), fieldName, TableStrings.TABLE_NAME, TableStrings.rangeId, lastStoredRangeId);
    }

    protected void endCurrentSensor() throws Exception {
        this.endTimestampUTC = System.currentTimeMillis();
        this.CRC = computeCRC32();
        this.rangeId = getLastStoredRangeId();

        String sql = String.format("UPDATE %s SET %s=?, %s=? WHERE %s=?", TableStrings.TABLE_NAME,
                TableStrings.endTimestampUTC,
                TableStrings.CRC,
                TableStrings.rangeId);

        try (SQLiteStatement statement = db.getSQLite().compileStatement(sql)) {
            statement.bindLong(1, endTimestampUTC);
            statement.bindLong(2, CRC);
            statement.bindLong(3, rangeId);
            statement.execute();
        }

        this.onTableChanged();
    }

    public void patchWithNewSensor() throws Exception {
        // а вот когда мы стартуем новый сенсор,
        // вот тогда надо закончить сенсор здесь.
        // см. private void onSensorEnded() класса SensorTable.
        if(!isTableNull()) {
            this.endCurrentSensor();
        }
        this.createNewSensorRecord();
    }

    private Integer getLastStoredRangeId() {
        return SqlUtils.getLastStoredFieldValue(db.getSQLite(), TableStrings.rangeId, TableStrings.TABLE_NAME);
    }

    private void createNewSensorRecord() throws Exception {
        SqlUtils.validateTime(this, libreMessage);

        // В таблице LibreLink для действующего сенсора
        // значение конца времени действия равно
        // 9223372036854775807
        this.endTimestampUTC = Long.MAX_VALUE;
        // значение rangeId равно значению sensorId
        // rangeId писать НУЖНО, так как автоматически он НЕ увеличивается.
        this.rangeId = (this.getLastStoredRangeId() == null) ? 1 : this.getLastStoredRangeId() + 1;
        this.sensorId = db.getSensorTable().getLastStoredSensorId();
        this.startTimestampUTC = libreMessage.getSensorStartTimestampUTC();
        this.CRC = computeCRC32();

        ContentValues values = new ContentValues();
        values.put(TableStrings.endTimestampUTC, endTimestampUTC);
        values.put(TableStrings.rangeId, rangeId);
        values.put(TableStrings.sensorId, sensorId);
        values.put(TableStrings.startTimestampUTC, startTimestampUTC);
        values.put(TableStrings.CRC, CRC);

        db.getSQLite().insertOrThrow(TableStrings.TABLE_NAME, null, values);
        this.onTableChanged();
    }

    private long endTimestampUTC;
    private int rangeId;
    private int sensorId;
    private long startTimestampUTC;
    private long CRC;

    private static class TableStrings {
        final static String TABLE_NAME = "sensorSelectionRanges";
        final static String endTimestampUTC = "endTimestampUTC";
        final static String rangeId = "rangeId";
        final static String sensorId = "sensorId";
        final static String startTimestampUTC = "startTimestampUTC";
        final static String CRC = "CRC";
    }
}
