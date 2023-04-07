package com.example.nfc_libre_scan.librelink_db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SqliteSequence {
    private final SQLiteDatabase db;
    private final int users;
    private final int sensors;
    private final int rawScans;
    private final int sensorSelectionRanges;
    private final int realTimeReadings;
    private final int historicReadings;
    private final int historicErrors;

    public SqliteSequence(SQLiteDatabase db) {
        this.db = db;
        this.users = getUsers();
        this.sensors = getSensors();
        this.rawScans = getRawScans();
        this.sensorSelectionRanges = getSensorSelectionRanges();
        this.realTimeReadings = getRealTimeReadings();
        this.historicReadings = getHistoricReadings();
        this.historicErrors = getHistoricErrors();
    }

    private int getUsers() {
        return countStringsInTable(NameFields.users);
    }

    private int getSensors() {
        return countStringsInTable(NameFields.sensors);
    }

    private int getRawScans() {
        return countStringsInTable(NameFields.rawScans);
    }

    private int getSensorSelectionRanges() {
        return countStringsInTable(NameFields.sensorSelectionRanges);
    }

    private int getRealTimeReadings() {
        return countStringsInTable(NameFields.realTimeReadings);
    }

    private int getHistoricReadings() {
        return countStringsInTable(NameFields.historicReadings);
    }

    private int getHistoricErrors() {
        return countStringsInTable(NameFields.historicErrors);
    }

    private int countStringsInTable(String tableName) {
        String sql = String.format("SELECT COUNT(*) FROM %s;", tableName);
        Cursor cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void updateItselfInDb(){
        //updateValueInDB(TableNames.users, this.users);
        //updateValueInDB(TableNames.sensors, this.sensors);
        updateValueInDB(NameFields.rawScans, this.rawScans);
        //updateValueInDB(TableNames.sensorSelectionRanges, this.sensorSelectionRanges);
        updateValueInDB(NameFields.realTimeReadings, this.realTimeReadings);
        updateValueInDB(NameFields.historicReadings, this.historicReadings);
        //updateValueInDB(TableNames.historicErrors, this.historicErrors);
    }

    private void updateValueInDB(String tableName, int newValue){
        String sql = String.format("UPDATE sqlite_sequence SET seq = %s WHERE name='%s';", tableName, newValue);
        db.execSQL(sql);
    }

    private static class NameFields {
        final static String users = "users";
        final static String sensors = "sensors";
        final static String rawScans = "rawScans";
        final static String sensorSelectionRanges = "sensorSelectionRanges";
        final static String realTimeReadings = "realTimeReadings";
        final static String historicReadings = "historicReadings";
        final static String historicErrors = "historicErrors";
    }
}
