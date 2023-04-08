package com.example.nfc_libre_scan;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.librelink_sas_db.RawScanTable;
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
            boolean a = testRawScan(db);
            boolean b = testSensor(db);
            db.close();
            return a && b;
        } catch (IOException ignored) {
            return false;
        }
    }

    private boolean testSensor(SQLiteDatabase db) throws IOException {
        final long rightCRC = 2079731897;
        SensorTable sensorTable = new SensorTable(db);
        sensorTable.fillClassByValuesInLastSensorRecord();
        final long calculatedCRC = sensorTable.getComputedCRC();
        return calculatedCRC == rightCRC;
    }

    private boolean testRawScan(SQLiteDatabase db) throws IOException {
        final long rightCRC = 1875493694;

        RawScanTable rawScanTableRecord = new RawScanTable(db);
        rawScanTableRecord.fillClassByValuesInLastRawScanRecord();
        final long calculatedCRC = rawScanTableRecord.getComputedCRC();
        return calculatedCRC == rightCRC;
    }
}
