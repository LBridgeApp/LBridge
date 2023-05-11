package com.diabetes.lbridge.librelink.librelink_sas_db.tables;

import com.diabetes.lbridge.Utils;
import com.diabetes.lbridge.librelink.librelink_sas_db.LibreLinkDatabase;
import com.diabetes.lbridge.libre.LibreMessage;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.HistoricReadingRow;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.ScanTimeRow;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.TimeRow;
import com.oop1.GlucoseUnit;
import com.oop1.HistoricBg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoricReadingTable implements Table, TimeTable, ScanTimeTable {
    private final LibreLinkDatabase db;
    private HistoricReadingRow[] rows;
    public HistoricReadingTable(LibreLinkDatabase db) {
        this.db = db;
        this.rows = this.queryRows();
    }

    public int getLastStoredSampleNumber(){
        return (rows.length != 0) ? rows[rows.length - 1].getSampleNumber() : 0;
    }

    public void addLastSensorScan(LibreMessage libreMessage) throws Exception {

        final int lastStoredSampleNumber = getLastStoredSampleNumber();

        HistoricBg[] missingHistoricBgs = Arrays.stream(libreMessage.getHistoricBgs())
                .filter(bg -> bg.getSampleNumber() > lastStoredSampleNumber)
                .toArray(HistoricBg[]::new);

        for(HistoricBg missedHistoricBg : missingHistoricBgs){
            this.addNewHistoricBG(missedHistoricBg);
        }
    }

    private void addNewHistoricBG(HistoricBg historicBg) throws Exception {

        double glucoseValue = historicBg.convertBG(GlucoseUnit.MGDL).getBG();
        int sampleNumber = historicBg.getSampleNumber();
        int sensorId = db.getSensorTable().getLastSensorId();
        // TODO: Непонятно, когда timeChangeBefore может быть не равен нулю.
        int timeChangeBefore = 0;
        String timeZone = historicBg.getTimeZone();
        long timestampLocal = Utils.withoutNanos(historicBg.getTimestampLocal());
        long timestampUTC = Utils.withoutNanos(historicBg.getTimestampUTC());

        HistoricReadingRow row = new HistoricReadingRow(this, glucoseValue, sampleNumber,
                sensorId, timeChangeBefore, timeZone, timestampLocal, timestampUTC);
        row.insertOrThrow();
    }

    public int getLastStoredReadingId(){
        return (rows.length != 0) ? rows[rows.length - 1].getReadingId() : 0;
    }

    @Override
    public String getName() {
        return "historicReadings";
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return db;
    }

    @Override
    public HistoricReadingRow[] queryRows(){

        List<HistoricReadingRow> rowList = new ArrayList<>();

        int rowLength = Table.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new HistoricReadingRow(this, rowIndex));
        }

        return rowList.toArray(new HistoricReadingRow[0]);
    }

    @Override
    public void rowInserted() {
        rows = this.queryRows();
    }

    @Override
    public long getBiggestTimestampUTC() {
        long biggestTimestamp = 0;

        for(TimeRow row : rows){
            biggestTimestamp = Math.max(biggestTimestamp, row.getBiggestTimestampUTC());
        }
        return biggestTimestamp;
    }

    @Override
    public long getBiggestScanTimestampUTC() {
        long biggestScanTimestamp = 0;

        for(ScanTimeRow row : rows){
            biggestScanTimestamp = Math.max(biggestScanTimestamp, row.getScanTimestampUTC());
        }
        return biggestScanTimestamp;
    }
}
