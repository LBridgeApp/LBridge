package com.diabetes.lbridge.librelink.librelink_sas_db.tables;

import android.database.Cursor;

import com.diabetes.lbridge.Logger;
import com.diabetes.lbridge.Utils;
import com.diabetes.lbridge.App;
import com.diabetes.lbridge.AppDatabase;
import com.diabetes.lbridge.libre.LibreMessage;
import com.diabetes.lbridge.libre.PatchUID;
import com.diabetes.lbridge.librelink.librelink_sas_db.LibreLinkDatabase;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.ScanTimeRow;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.SensorRow;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.TimeRow;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorTable implements Table, TimeTable, ScanTimeTable {

    private final LibreLinkDatabase db;
    private final AppDatabase appDatabase;
    private SensorRow[] rows;

    public SensorTable(LibreLinkDatabase db) {
        this.db = db;
        this.appDatabase = App.getInstance().getAppDatabase();
        this.rows = this.queryRows();
    }

    public void updateToLastScan(LibreMessage libreMessage) throws Exception {

        String originalSN = libreMessage.getLibreSN();
        String sensorAlias = getSensorAlias(libreMessage.getLibreSN());

        Logger.inf(String.format("Original serial number: %s." + "Sensor alias: %s", originalSN, sensorAlias));

        if (sensorAlias == null) {
            Logger.inf(String.format("Sensor alias for sensor with serial number %s does not exists in our db. Adding...", libreMessage.getLibreSN()));
            this.setSensorAlias(libreMessage.getLibreSN(), libreMessage.getLibreSN());
            Logger.ok("Alias added");
        }

        boolean isSensorAliasExists = isSensorExists(getSensorAlias(originalSN));
        boolean isSensorAliasExpired = isSensorExpired(originalSN);

        if (!isSensorAliasExists) {
            // если сенсор новый или всё-таки продленный, но отсутствует в базе.
            String originalLibreSN = libreMessage.getLibreSN();

            Logger.inf(String.format("Sensor with serial number %s and alias %s does not exists in db. " +
                    "Creating new sensor record. Sensor alias is %s", originalLibreSN, originalLibreSN, originalLibreSN));

            byte[] originalPatchUID = libreMessage.getRawLibreData().getPatchUID();

            this.setSensorAlias(originalLibreSN, originalLibreSN);

            this.createNewSensorRecord(libreMessage, originalLibreSN, originalPatchUID);

            long sensorStartTimestampUTC = db.getLibreMessage().getSensorStartTimestampUTC();
            db.getSensorSelectionRangeTable().createNewSensorRecord(sensorStartTimestampUTC);

            Logger.ok("New record added.");

        } else if (isSensorAliasExpired) {
            // если срок действия сенсора истёк

            String originalLibreSN = libreMessage.getLibreSN();

            String expiredAlias = getSensorAlias(originalLibreSN);

            byte[] fakePatchUID = PatchUID.generateFake();

            String fakeLibreSN = PatchUID.decodeSerialNumber(fakePatchUID);

            Logger.inf(String.format("Sensor with serial number %s and alias %s expired in db. " +
                            "Creating new sensor record with fake alias %s...",
                    originalLibreSN, expiredAlias, fakeLibreSN));

            this.setSensorAlias(originalLibreSN, fakeLibreSN);

            db.getSensorSelectionRangeTable().endSensor(expiredAlias);

            this.createNewSensorRecord(libreMessage, fakeLibreSN, fakePatchUID);

            long sensorStartTimestampUTC = db.getLibreMessage().getSensorStartTimestampUTC();
            db.getSensorSelectionRangeTable().createNewSensorRecord(sensorStartTimestampUTC);

            Logger.ok("New record added.");

        } else {
            String originalLibreSN = libreMessage.getLibreSN();
            String alias = getSensorAlias(libreMessage.getLibreSN());
            Logger.inf(String.format("Updating sensor record with serial number %s and alias %s ...",
                    originalLibreSN, alias));

            this.updateSensorRecord(libreMessage, alias);
            Logger.ok("Record updated.");
        }
    }

    private void createNewSensorRecord(LibreMessage libreMessage, String serialNumber, byte[] uniqueIdentifier) throws Exception {
        byte[] attenuationState = libreMessage.getLibreSavedState().getAttenuationState();
        byte[] bleAddress = null;
        byte[] compositeState = libreMessage.getLibreSavedState().getCompositeState();
        int enableStreamingTimestamp = 0;
        boolean endedEarly = false;
        byte[] initialPatchInformation = libreMessage.getRawLibreData().getPatchInfo();
        int lastScanSampleNumber = libreMessage.getCurrentBg().getSampleNumber();
        String lastScanTimeZone = libreMessage.getCurrentBg().getTimeZone();
        long lastScanTimestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        long lastScanTimestampUTC = libreMessage.getCurrentBg().getTimestampUTC();
        boolean lsaDetected = false;
        // TODO: непонятно, как считать measurementState
        byte[] measurementState = null;
        int personalizationIndex = 0;
        String sensorStartTimeZone = libreMessage.getCurrentBg().getTimeZone();
        long sensorStartTimestampLocal = Utils.withoutNanos(libreMessage.getSensorStartTimestampLocal());
        long sensorStartTimestampUTC = Utils.withoutNanos(libreMessage.getSensorStartTimestampUTC());
        byte[] streamingAuthenticationData = null;
        int streamingUnlockCount = 0;
        int unrecordedHistoricTimeChange = 0;
        int unrecordedRealTimeTimeChange = 0;
        int userId = db.getUserTable().getLastStoredUserId();
        int warmupPeriodInMinutes = 60;
        int wearDurationInMinutes = 20160;

        SensorRow row = new SensorRow(this, attenuationState, bleAddress, compositeState,
                enableStreamingTimestamp, endedEarly, initialPatchInformation,
                lastScanSampleNumber, lastScanTimeZone, lastScanTimestampLocal, lastScanTimestampUTC,
                lsaDetected, measurementState, personalizationIndex, sensorStartTimeZone,
                sensorStartTimestampLocal, sensorStartTimestampUTC, serialNumber,
                streamingAuthenticationData, streamingUnlockCount, uniqueIdentifier,
                unrecordedHistoricTimeChange, unrecordedRealTimeTimeChange,
                userId, warmupPeriodInMinutes, wearDurationInMinutes);
        row.insertOrThrow();
    }

    private void updateSensorRecord(LibreMessage libreMessage, String libreAlias) throws Exception {

        SensorRow row = Arrays.stream(rows)
                .filter(r -> r.getSerialNumber().equals(libreAlias))
                .findFirst()
                .orElse(null);

        if (row == null) {
            throw new Exception(String.format("Row with alias %s not found.", libreAlias));
        }

        final byte[] messageAttenuationState = libreMessage.getLibreSavedState().getAttenuationState();
        final byte[] messageCompositeState = libreMessage.getLibreSavedState().getCompositeState();

        final byte[] tableAttenuationState = row.getAttenuationState();

        final byte[] tableCompositeState = row.getCompositeState();

        // Если в таблице LibreLink attenuationState и compositeState
        // не равны null, а в libreMessage равны null, то не перезаписывать.
        byte[] attenuationState = (messageAttenuationState != null) ? messageAttenuationState : tableAttenuationState;
        byte[] compositeState = (messageCompositeState != null) ? messageCompositeState : tableCompositeState;

        int lastScanSampleNumber = libreMessage.getCurrentBg().getSampleNumber();
        String lastScanTimeZone = libreMessage.getCurrentBg().getTimeZone();
        long lastScanTimestampLocal = libreMessage.getCurrentBg().getTimestampLocal();
        long lastScanTimestampUTC = libreMessage.getCurrentBg().getTimestampUTC();

        row.setAttenuationState(attenuationState)
                .setCompositeState(compositeState)
                .setLastScanSampleNumber(lastScanSampleNumber)
                .setLastScanTimeZone(lastScanTimeZone)
                .setLastScanTimestampLocal(lastScanTimestampLocal)
                .setLastScanTimestampUTC(lastScanTimestampUTC)
                .replace();
    }

    public int getLastSensorId() {
        return (rows.length != 0) ? rows[rows.length - 1].getSensorId() : 0;
    }

    public String getLastSensorSerialNumber() {
        return (rows.length != 0) ? rows[rows.length - 1].getSerialNumber() : null;
    }

    private void setFakeSerialNumberForSensor(String libreSN) throws Exception {

        SensorRow sensorRow = Arrays.stream(rows).filter(row -> row.getSerialNumber().equals(libreSN))
                .findFirst().orElseThrow(() -> new Exception(String.format("Sensor with serial number %s not found.", libreSN)));

        byte[] fakePatchUID = PatchUID.generateFake();
        String fakeLibreSN = PatchUID.decodeSerialNumber(fakePatchUID);
        sensorRow.setSerialNumber(fakeLibreSN).setUniqueIdentifier(fakePatchUID).replace();
    }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        String serialNumberOfLastSensor = this.getLastSensorSerialNumber();
        this.setFakeSerialNumberForSensor(serialNumberOfLastSensor);
    }

    private void endSensor(String libreSN) throws Exception {

        SensorRow sensorRow = Arrays.stream(rows).filter(row -> row.getSerialNumber().equals(libreSN))
                .findFirst().orElseThrow(() -> new Exception(String.format("Sensor with serial number %s not found.", libreSN)));

        long sensorStartTimestampUTC = Utils.withoutNanos(LocalDateTime.now().minusDays(15).toInstant(ZoneOffset.UTC).toEpochMilli());
        long sensorStartTimestampLocal = Utils.withoutNanos(Utils.unixAsLocal(sensorStartTimestampUTC));

        sensorRow.setSensorStartTimestampLocal(sensorStartTimestampLocal)
                .setSensorStartTimestampUTC(sensorStartTimestampUTC)
                .setEndedEarly(false)
                .replace();

        this.onSensorEnded();
    }

    public void endLastSensor() throws Exception {
        String serialNumberOfLastSensor = this.getLastSensorSerialNumber();
        this.endSensor(serialNumberOfLastSensor);
    }

    private boolean isSensorExpired(String libreSN) throws Exception {
        if (libreSN == null) {
            throw new Exception("LibreSN is null");
        }

        SensorRow sensorRow = Arrays.stream(rows)
                .filter(row -> row.getSerialNumber().equals(libreSN))
                .findFirst().orElse(null);
        if (sensorRow == null) {
            return false;
        }

        long sensorStartTimestampUTC = sensorRow.getSensorStartTimestampUTC();
        boolean endedEarly = sensorRow.getEndedEarly();

        Instant startInstant = Instant.ofEpochMilli(sensorStartTimestampUTC);
        Instant endInstant = startInstant.plus(14, ChronoUnit.DAYS);
        long sensorExpirationTimestamp = endInstant.toEpochMilli();

        long currentTimestamp = System.currentTimeMillis();

        return currentTimestamp >= sensorExpirationTimestamp || endedEarly;
    }

    private boolean isSensorExists(String libreSN) throws Exception {
        if (libreSN == null) {
            throw new Exception("LibreSN is null");
        }

        SensorRow sensorRow = Arrays.stream(rows)
                .filter(row -> row.getSerialNumber().equals(libreSN))
                .findFirst().orElse(null);
        return sensorRow != null;
    }

    private void onSensorEnded() {
        // путем опытов пришел к выводу,
        // что здесь не нужно заканчивать сенсор
        // в таблице SensorSelectionRanges.
        // На самом деле, так лучше,
        // потому что в приложении будет надпись,
        // что работа сенсора закончена.
        // Если в SensorSelectionRanges сенсор закончить,
        // то красная надпись пропадет, а будет просто желтая полоса
        // с предложением отсканировать новый сенсор.
    }

    private String getSensorAlias(String originalLibreSN) {
        String sql = "SELECT fakeSN FROM sensorAliases WHERE originalLibreSN = ?";
        Cursor cursor = App.getInstance().getAppDatabase().getSQLite().rawQuery(sql, new String[]{originalLibreSN});
        String aliasSN = null;
        if (cursor.moveToFirst()) {
            aliasSN = cursor.getString(0);
        }

        cursor.close();
        return aliasSN;
    }

    private void setSensorAlias(String originalLibreSN, String sensorAlias) throws Exception {
            appDatabase.execInTransaction(() -> {
                appDatabase.getSQLite().execSQL("INSERT OR REPLACE INTO sensorAliases (originalLibreSN, fakeSN) VALUES (?, ?);",
                        new String[]{originalLibreSN, sensorAlias});
            });
            Logger.ok(String.format("Set alias for sensor serial number %s, alias is %s", originalLibreSN, sensorAlias));
    }

    @Override
    public String getName() {
        return "sensors";
    }

    @Override
    public SensorRow[] queryRows() {
        List<SensorRow> rowList = new ArrayList<>();

        int rowLength = Table.getRowLength(db.getSQLite(), this);
        for (int rowIndex = 0; rowIndex < rowLength; rowIndex++) {
            rowList.add(new SensorRow(this, rowIndex));
        }

        return rowList.toArray(new SensorRow[0]);
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
}
