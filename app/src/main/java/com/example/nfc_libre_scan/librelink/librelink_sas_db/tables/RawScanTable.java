package com.example.nfc_libre_scan.librelink.librelink_sas_db.tables;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.LibreLinkDatabase;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.RawScanRow;

import java.util.ArrayList;
import java.util.List;

public class RawScanTable implements Table {
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
}
