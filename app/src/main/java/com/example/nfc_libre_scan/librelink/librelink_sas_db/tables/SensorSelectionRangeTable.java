package com.example.nfc_libre_scan.librelink.librelink_sas_db.tables;

import com.example.nfc_libre_scan.librelink.librelink_sas_db.LibreLinkDatabase;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.SensorRow;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.SensorSelectionRangeRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorSelectionRangeTable implements Table {

    private final LibreLinkDatabase db;
    private SensorSelectionRangeRow[] rows;

    public SensorSelectionRangeTable(LibreLinkDatabase db) {
        this.db = db;
        this.rows = this.queryRows();
    }

    protected void endSensor(String libreSN) throws Exception {

        SensorTable sensorTable = db.getSensorTable();
        SensorRow[] sensorRows = sensorTable.queryRows();
        SensorRow sensorRow = Arrays.stream(sensorRows)
                .filter(row -> row.getSerialNumber().equals(libreSN))
                .findFirst()
                .orElseThrow(() -> new Exception(String.format("Sensor with serial number %s not found.", libreSN)));

        SensorSelectionRangeRow rangeRow = Arrays.stream(rows)
                .filter(row -> row.getSensorId() == sensorRow.getSensorId())
                .findFirst()
                .orElseThrow(() -> new Exception(String.format("Sensor with serial number %s not found.", libreSN)));

        rangeRow.setEndTimestampUTC(System.currentTimeMillis()).replace();
    }

    /*public void patchWithNewSensor(String oldSensorSN) throws Exception {
        // а вот когда мы стартуем новый сенсор,
        // вот тогда надо закончить сенсор здесь.
        // см. private void onSensorEnded() класса SensorTable.
        if (rows.length != 0) {
            this.endSensor(oldSensorSN);
        }
        this.createNewSensorRecord(db.getLibreMessage());
    }*/

    protected void createNewSensorRecord(long sensorStartTimestampUTC) throws Exception {

        // В таблице LibreLink для действующего сенсора
        // значение конца времени действия равно
        // 9223372036854775807
        long endTimestampUTC = Long.MAX_VALUE;
        // значение rangeId равно значению sensorId
        // rangeId писать НУЖНО, так как автоматически он НЕ увеличивается.

        int rangeId = getLastStoredRangeId() + 1;

        int sensorId = db.getSensorTable().getLastSensorId();

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
}
