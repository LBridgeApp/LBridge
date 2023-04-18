package com.example.nfc_libre_scan;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.librelink_sas_db.HistoricReadingTable;
import com.example.nfc_libre_scan.librelink_sas_db.RawScanTable;
import com.example.nfc_libre_scan.librelink_sas_db.RealTimeReadingTable;
import com.example.nfc_libre_scan.librelink_sas_db.SensorTable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class AppTester {
    private final String databaseName = "testDatabase.db";
    private final Activity activity;
    private final Logger logger;

    AppTester(Activity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
    }

    private void createTestDatabase() throws IOException {
        logger.inf("Creating test db...");
        Path testDBFilePath = activity.getDatabasePath(databaseName).toPath();

        OutputStream testDBFileStream = Files.newOutputStream(testDBFilePath);

        InputStream assetsFileStream = activity.getAssets().open(databaseName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = assetsFileStream.read(buffer)) > 0) {
            testDBFileStream.write(buffer, 0, length);
        }

        testDBFileStream.flush();
        testDBFileStream.close();
        assetsFileStream.close();
        logger.ok("Test db created");
    }

    public boolean runTests() {
        try {
            createTestDatabase();
            SQLiteDatabase db = SQLiteDatabase.openDatabase(
                    activity.getDatabasePath(databaseName).getPath(),
                    null, SQLiteDatabase.OPEN_READONLY);
            boolean a = testRawScanTable(db);
            boolean b = testSensorTable(db);
            boolean c = testRealTimeReadingTable(db);
            boolean d = testHistoricReadingTable(db);
            db.close();
            return a && b && c && d;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean testSensorTable(SQLiteDatabase db) throws Exception {
        final long rightCRC = 2079731897;
        SensorTable sensorTable = new SensorTable(db, null);
        sensorTable.fillClassByValuesInLastSensorRecord();
        final long calculatedCRC = sensorTable.getComputedCRC();
        return calculatedCRC == rightCRC;
    }

    private boolean testRawScanTable(SQLiteDatabase db) throws Exception {
        final long rightCRC = 1875493694;

        RawScanTable rawScanTable = new RawScanTable(db, null);
        rawScanTable.fillClassByValuesInLastRawScanRecord();
        final long calculatedCRC = rawScanTable.getComputedCRC();
        return calculatedCRC == rightCRC;
    }

    private boolean testRealTimeReadingTable(SQLiteDatabase db) throws Exception {
        final long rightCRC = 2691565280L;

        RealTimeReadingTable realTimeReadingTable = new RealTimeReadingTable(db, null);
        realTimeReadingTable.fillClassByValuesInLastRealTimeReadingRecord();
        final long calculatedCRC = realTimeReadingTable.getComputedCRC();
        return calculatedCRC == rightCRC;
    }

    private boolean testHistoricReadingTable(SQLiteDatabase db) throws Exception {
        final long rightCRC = 3219851232L;

        HistoricReadingTable historicReadingTable = new HistoricReadingTable(db, null);
        historicReadingTable.fillClassByValuesInLastHistoricReadingRecord();
        final long calculatedCRC = historicReadingTable.getComputedCRC();
        return calculatedCRC == rightCRC;
    }
}
