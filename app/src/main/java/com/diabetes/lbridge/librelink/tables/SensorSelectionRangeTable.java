package com.diabetes.lbridge.librelink.tables;

import com.diabetes.lbridge.Logger;
import com.diabetes.lbridge.librelink.LibreLinkDatabase;
import com.diabetes.lbridge.librelink.rows.CrcRow;
import com.diabetes.lbridge.librelink.rows.SensorRow;
import com.diabetes.lbridge.librelink.rows.SensorSelectionRangeRow;
import com.diabetes.lbridge.librelink.rows.TimeRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorSelectionRangeTable implements Table, TimeTable, CrcTable {

    private final LibreLinkDatabase db;
    private SensorSelectionRangeRow[] rows;

    public SensorSelectionRangeTable(LibreLinkDatabase db) {
        this.db = db;
        this.rows = this.queryRows();
    }

    protected SensorSelectionRangeRow getSensorRow(String libreSN) throws Exception {
        SensorRow sensorRow = db.getSensorTable().getSensorRow(libreSN);

        return Arrays.stream(rows)
                .filter(row -> row.getSensorId() == sensorRow.getSensorId())
                .findFirst()
                .orElseThrow(() -> new Exception(String.format("Sensor with serial number %s not found in sensor selection table.", libreSN)));
    }

    protected void endSensor(String libreSN) throws Exception {
        Logger.inf(String.format("Ending old sensor %s ...", libreSN));
        // Сенсор здесь нужно заканчивать только при старте нового сенсора.

        SensorSelectionRangeRow rangeRow = this.getSensorRow(libreSN);
        rangeRow.setEndTimestampUTC(System.currentTimeMillis()).replace();

        Logger.inf(String.format("Old sensor %s ended.", libreSN));
    }

    protected void createNewSensorRecord(long sensorStartTimestampUTC) throws Exception {

        // В таблице LibreLink для действующего и истекшего сенсора
        // значение конца времени действия равно
        // 9223372036854775807
        long endTimestampUTC = Long.MAX_VALUE;
        // значение rangeId равно значению sensorId
        // rangeId писать НУЖНО, так как автоматически он НЕ увеличивается.

        int rangeId = getLastStoredRangeId() + 1;

        int sensorId = db.getSensorTable().getLastSensorId();

        if(rangeId != sensorId){
            throw new Exception("RangeId not equals sensorId");
        }

        SensorSelectionRangeRow row = new SensorSelectionRangeRow(this,
                endTimestampUTC, rangeId, sensorId, sensorStartTimestampUTC);
        row.insertOrThrow();
    }

    public int getLastStoredRangeId() {
        return (rows.length != 0) ? rows[rows.length - 1].getRangeId() : 0;
    }

    @Override
    public String getName() {
        return "sensorSelectionRanges";
    }

    @Override
    public SensorSelectionRangeRow[] queryRows() {
        List<SensorSelectionRangeRow> rowList = new ArrayList<>();

        int rowLength = Table.getRowLength(db.getSQLite(), this);
        for (int rowIndex = 0; rowIndex < rowLength; rowIndex++) {
            rowList.add(new SensorSelectionRangeRow(this, rowIndex));
        }

        return rowList.toArray(new SensorSelectionRangeRow[0]);
    }

    @Override
    public void rowInserted() {
        this.rows = this.queryRows();
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return db;
    }

    @Override
    public long getBiggestTimestampUTC() {
        long biggestTimestamp = 0;

        for (TimeRow row : rows) {
            biggestTimestamp = Math.max(biggestTimestamp, row.getBiggestTimestampUTC());
        }
        return biggestTimestamp;
    }

    @Override
    public void validateCRC() throws Exception {
        for (CrcRow row : rows) {
            row.validateCRC();
        }
    }
}
