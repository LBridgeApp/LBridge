package com.example.nfc_libre_scan.librelink.librelink_sas_db.tables;

import com.example.nfc_libre_scan.Utils;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.LibreLinkDatabase;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.RealTimeReadingRow;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.ScanTimeRow;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.TimeRow;
import com.oop1.GlucoseUnit;

import java.util.ArrayList;
import java.util.List;

public class RealTimeReadingTable implements Table, TimeTable, ScanTimeTable {
    private final LibreLinkDatabase db;
    private RealTimeReadingRow[] rows;
    public RealTimeReadingTable(LibreLinkDatabase db) {
        this.db = db;
        this.rows = this.queryRows();
    }

    public void addLastSensorScan(LibreMessage libreMessage) throws Exception {

        double glucoseValue = libreMessage.getCurrentBg().convertBG(GlucoseUnit.MGDL).getBG();
        boolean isActionable = true;
        double rateOfChange = 0.0;
        int sensorId = db.getSensorTable().getLastSensorId();
        long timeChangeBefore = 0;
        String timeZone = libreMessage.getCurrentBg().getTimeZone();
        long timestampLocal = Utils.withoutNanos(libreMessage.getCurrentBg().getTimestampLocal());
        long timestampUTC = Utils.withoutNanos(libreMessage.getCurrentBg().getTimestampUTC());
        int trendArrow = libreMessage.getCurrentBg().getCurrentTrend().toValue();

        RealTimeReadingRow row = new RealTimeReadingRow(this, libreMessage, glucoseValue,
                isActionable, rateOfChange, sensorId, timeChangeBefore, timeZone,
                timestampLocal, timestampUTC, trendArrow);
        row.insertOrThrow();
    }

    @Override
    public String getName() {
        return "realTimeReadings";
    }

    @Override
    public RealTimeReadingRow[] queryRows() {
        List<RealTimeReadingRow> rowList = new ArrayList<>();

        int rowLength = Table.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new RealTimeReadingRow(this, rowIndex));
        }

        return rowList.toArray(new RealTimeReadingRow[0]);
    }

    @Override
    public void rowInserted() {
        this.rows = this.queryRows();
    }

    public int getLastStoredReadingId(){
        return (rows.length != 0) ? rows[rows.length - 1].getReadingId() : 0;
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return db;
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
