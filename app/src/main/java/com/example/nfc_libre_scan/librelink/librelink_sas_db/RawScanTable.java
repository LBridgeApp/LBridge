package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;

import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.HistoricReadingRow;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.RawScanRow;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class RawScanTable implements Table {
    private final LibreLinkDatabase db;

    public RawScanTable(LibreLinkDatabase db) {
        this.db = db;
    }

    public void addLastSensorScan(LibreMessage libreMessage) throws Exception {

        byte[] patchInfo = libreMessage.getRawLibreData().getPatchInfo();
        byte[] payload = libreMessage.getRawLibreData().getPayload();
        int sensorId = db.getSensorTable().getLastStoredSensorId();
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
        RawScanRow[] rows = this.queryRows();
        return rows[rows.length - 1].getScanId();
    }

    @Override
    public RawScanRow[] queryRows() {
        List<RawScanRow> rowList = new ArrayList<>();

        int rowLength = SqlUtils.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new RawScanRow(this, rowIndex));
        }

        return rowList.toArray(new RawScanRow[0]);
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return db;
    }
}
