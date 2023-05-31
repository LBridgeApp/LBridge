package com.diabetes.lbridge.librelink.tables;

import com.diabetes.lbridge.libre.LibreMessage;
import com.diabetes.lbridge.librelink.LibreLinkDatabase;
import com.diabetes.lbridge.librelink.rows.CrcRow;
import com.diabetes.lbridge.librelink.rows.RawScanRow;
import com.diabetes.lbridge.librelink.rows.ScanTimeRow;
import com.diabetes.lbridge.librelink.rows.TimeRow;

import java.util.ArrayList;
import java.util.List;

public class RawScanTable implements Table, TimeTable, ScanTimeTable, CrcTable {
    private final LibreLinkDatabase db;
    private RawScanRow[] rows;

    public RawScanTable(LibreLinkDatabase db) {
        this.db = db;
        this.rows = queryRows();
    }

    public void addLastSensorScan(LibreMessage libreMessage) throws Exception {

        byte[] patchInfo = libreMessage.getRawLibreData().getPatchInfo();
        byte[] payload = libreMessage.getRawLibreData().getPayload();
        int sensorId = db.getSensorTable().getLastSensorId();
        String timeZone = libreMessage.getCurrentBg().getTimeZone();
        long timestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        long timestampUTC = libreMessage.getCurrentBg().getTimestampUTC();

        RawScanRow row = new RawScanRow(this, patchInfo,
                payload, sensorId, timeZone, timestampLocal, timestampUTC);
        row.insertOrThrow();
    }

    @Override
    public String getName() {
        return "rawScans";
    }

    public int getLastStoredScanId(){
        return (rows.length != 0) ? rows[rows.length - 1].getScanId() : 0;
    }

    @Override
    public RawScanRow[] queryRows() {
        List<RawScanRow> rowList = new ArrayList<>();

        int rowLength = Table.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new RawScanRow(this, rowIndex));
        }

        return rowList.toArray(new RawScanRow[0]);
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

    @Override
    public void validateCRC() throws Exception {
        for(CrcRow row : rows){
            row.validateCRC();
        }
    }
}
