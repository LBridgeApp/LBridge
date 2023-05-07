package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.ContentValues;

import com.example.nfc_libre_scan.Utils;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.HistoricReadingRow;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.RealTimeReadingRow;
import com.oop1.GlucoseUnit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class RealTimeReadingTable implements Table {
    private final LibreLinkDatabase db;
    public RealTimeReadingTable(LibreLinkDatabase db) {
        this.db = db;
    }

    public void addLastSensorScan(LibreMessage libreMessage) throws Exception {

        double glucoseValue = libreMessage.getCurrentBg().convertBG(GlucoseUnit.MGDL).getBG();
        boolean isActionable = true;
        double rateOfChange = 0.0;
        int sensorId = db.getSensorTable().getLastStoredSensorId();
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

        int rowLength = SqlUtils.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new RealTimeReadingRow(this, rowIndex));
        }

        return rowList.toArray(new RealTimeReadingRow[0]);
    }

    public int getLastStoredReadingId(){
        RealTimeReadingRow[] rows = this.queryRows();
        return rows[rows.length - 1].getReadingId();
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return null;
    }
}
