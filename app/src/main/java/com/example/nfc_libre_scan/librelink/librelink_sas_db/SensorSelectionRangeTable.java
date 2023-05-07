package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.database.sqlite.SQLiteStatement;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.SensorSelectionRangeRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorSelectionRangeTable implements Table {

    private final LibreLinkDatabase db;
    private final LibreMessage libreMessage;

    SensorSelectionRangeTable(LibreLinkDatabase db) {
        this.db = db;
        this.libreMessage = db.getLibreMessage();
    }

    protected void endSensor(String libreSN) throws Exception {

        SensorSelectionRangeRow[] rows = this.queryRows();
        SensorTable sensorTable = db.getSensorTable();
        int lastSensorId = sensorTable.getLastStoredSensorId();

        SensorSelectionRangeRow sensorRow = Arrays.stream(rows).filter(t -> t.getSensorId() == lastSensorId)
                .findFirst().orElseThrow(() -> new Exception(String.format("Sensor with serial number %s not found.", libreSN)));

        sensorRow.setEndTimestampUTC(System.currentTimeMillis()).replace();
    }

    public void patchWithNewSensor() throws Exception {
        // а вот когда мы стартуем новый сенсор,
        // вот тогда надо закончить сенсор здесь.
        // см. private void onSensorEnded() класса SensorTable.
        SensorSelectionRangeRow[] rows = this.queryRows();
        if (rows.length == 0) {
            String lastSensorSerialNumber = db.getSensorTable().getLastSensorSerialNumber();
            this.endSensor(lastSensorSerialNumber);
        }
        this.createNewSensorRecord();
    }

    private void createNewSensorRecord() throws Exception {

        // В таблице LibreLink для действующего сенсора
        // значение конца времени действия равно
        // 9223372036854775807
        long endTimestampUTC = Long.MAX_VALUE;
        // значение rangeId равно значению sensorId
        // rangeId писать НУЖНО, так как автоматически он НЕ увеличивается.

        SensorSelectionRangeRow[] rows = this.queryRows();
        int rangeId = rows[rows.length - 1].getRangeId() + 1;

        int sensorId = db.getSensorTable().getLastStoredSensorId();
        long startTimestampUTC = libreMessage.getSensorStartTimestampUTC();

        SensorSelectionRangeRow row = new SensorSelectionRangeRow(this,
                endTimestampUTC, rangeId, sensorId, startTimestampUTC);
        row.insertOrThrow();
    }

    @Override
    public String getName() {
        return "sensorSelectionRanges";
    }

    @Override
    public SensorSelectionRangeRow[] queryRows() {
        List<SensorSelectionRangeRow> rowList = new ArrayList<>();

        int rowLength = SqlUtils.getRowLength(db.getSQLite(), this);
        for (int rowIndex = 0; rowIndex < rowLength; rowIndex++) {
            rowList.add(new SensorSelectionRangeRow(this, rowIndex));
        }

        return rowList.toArray(new SensorSelectionRangeRow[0]);
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return db;
    }
}
