package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.nfc_libre_scan.Logger;
import com.example.nfc_libre_scan.libre.LibreMessage;

public class LibreLinkDatabase {
    private final Context context;
    private final SQLiteDatabase db;
    private final LibreMessage libreMessage;

    private final HistoricReadingTable historicReadingTable;
    private final RawScanTable rawScanTable;
    private final RealTimeReadingTable realTimeReadingTable;
    private final SensorSelectionRangeTable sensorSelectionRangeTable;
    private final SensorTable sensorTable;
    private final UserTable userTable;

    public LibreLinkDatabase(Context context, LibreMessage libreMessage) throws Exception {
        this.db = SQLiteDatabase.openDatabase(context.getDatabasePath("sas.db").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
        this.context = context;
        this.libreMessage = libreMessage;

        historicReadingTable = new HistoricReadingTable(this);
        rawScanTable = new RawScanTable(this);
        realTimeReadingTable = new RealTimeReadingTable(this);
        sensorSelectionRangeTable = new SensorSelectionRangeTable(this);
        sensorTable = new SensorTable(this);
        userTable = new UserTable(this);

        historicReadingTable.onTableClassInit();
        rawScanTable.onTableClassInit();
        realTimeReadingTable.onTableClassInit();
        sensorSelectionRangeTable.onTableClassInit();
        sensorTable.onTableClassInit();
        userTable.onTableClassInit();
    }

    public void patchWithLastScan() throws Exception {
        try {
            db.beginTransaction();
            sensorTable.updateToLastScan();
            rawScanTable.addLastSensorScan();
            realTimeReadingTable.addLastSensorScan();
            historicReadingTable.addLastSensorScan();
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
        catch (Exception e){
            Logger.error(e);
            db.endTransaction();
            db.close();
            throw e;
        }
    }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        try {
            db.beginTransaction();
            this.sensorTable.setFakeSerialNumberForLastSensor();
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        } catch (Exception e){
            Logger.error(e);
            db.endTransaction();
            db.close();
            throw e;
        }
    }

    public void endCurrentSensor() throws Exception {
        try {
            db.beginTransaction();
            this.sensorTable.endCurrentSensor();
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
        catch (Exception e){
            Logger.error(e);
            db.endTransaction();
            db.close();
            throw e;
        }
    }

    protected SQLiteDatabase getSQLite(){
        return db;
    }

    protected LibreMessage getLibreMessage(){
        return libreMessage;
    }

    protected HistoricReadingTable getHistoricReadingTable(){
        return historicReadingTable;
    }

    protected RawScanTable getRawScanTable(){
        return rawScanTable;
    }

    protected RealTimeReadingTable getRealTimeReadingTable(){
        return realTimeReadingTable;
    }

    protected SensorSelectionRangeTable getSensorSelectionRangeTable(){
        return sensorSelectionRangeTable;
    }

    protected SensorTable getSensorTable(){
        return sensorTable;
    }

    protected UserTable getUserTable(){
        return userTable;
    }
}
