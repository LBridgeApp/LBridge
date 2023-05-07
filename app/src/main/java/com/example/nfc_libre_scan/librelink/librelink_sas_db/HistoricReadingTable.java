package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.Utils;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.HistoricReadingRow;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.SensorRow;
import com.oop1.GlucoseUnit;
import com.oop1.HistoricBg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoricReadingTable implements Table {
    private final LibreLinkDatabase db;
    public HistoricReadingTable(LibreLinkDatabase db) {
        this.db = db;
    }

    public void addLastSensorScan(LibreMessage libreMessage) throws Exception {
        //SqlUtils.validateTime(this, libreMessage);

        HistoricReadingRow[] rows = this.queryRows();

        final int lastStoredSampleNumber = rows[rows.length - 1].getSampleNumber();

        HistoricBg[] missingHistoricBgs = Arrays.stream(libreMessage.getHistoricBgs())
                .filter(bg -> bg.getSampleNumber() > lastStoredSampleNumber)
                .toArray(HistoricBg[]::new);

        for(HistoricBg missedHistoricBg : missingHistoricBgs){
            this.addNewHistoricBG(missedHistoricBg);
        }
    }

    private void addNewHistoricBG(HistoricBg historicBg) throws IOException {

        double glucoseValue = historicBg.convertBG(GlucoseUnit.MGDL).getBG();
        int sampleNumber = historicBg.getSampleNumber();
        int sensorId = db.getSensorTable().getLastStoredSensorId();
        int timeChangeBefore = 0;
        String timeZone = historicBg.getTimeZone();
        long timestampLocal = Utils.withoutNanos(historicBg.getTimestampLocal());
        long timestampUTC = Utils.withoutNanos(historicBg.getTimestampUTC());

        HistoricReadingRow row = new HistoricReadingRow(this, glucoseValue, sampleNumber,
                sensorId, timeChangeBefore, timeZone, timestampLocal, timestampUTC);
        row.insertOrThrow();
    }

    public int getLastStoredReadingId(){
        HistoricReadingRow[] rows = this.queryRows();
        return rows[rows.length - 1].getReadingId();
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

        int rowLength = SqlUtils.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new HistoricReadingRow(this, rowIndex));
        }

        return rowList.toArray(new HistoricReadingRow[0]);
    }
}
