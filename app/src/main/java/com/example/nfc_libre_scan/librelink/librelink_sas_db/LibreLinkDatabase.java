package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.nfc_libre_scan.App;
import com.example.nfc_libre_scan.Logger;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.LibreLink;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.tables.HistoricReadingTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.tables.RawScanTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.tables.RealTimeReadingTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.tables.SensorSelectionRangeTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.tables.SensorTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.tables.UserTable;

public class LibreLinkDatabase {
    private static final String APOLLO_DB = "apollo.db";
    private static final String SAS_DB = "sas.db";
    private SQLiteDatabase db;
    private HistoricReadingTable historicReadingTable;
    private RawScanTable rawScanTable;
    private RealTimeReadingTable realTimeReadingTable;
    private SensorSelectionRangeTable sensorSelectionRangeTable;
    private SensorTable sensorTable;
    private UserTable userTable;
    private final Context context;
    private final LibreLink librelink;

    public LibreLinkDatabase(Context context, LibreLink librelink) {
        this.context = context;
        this.librelink = librelink;

        // TODO: добавить валидацию времени и CRC
    }

    public void patchWithLastScan() throws Exception {
        performAction(() -> {
            sensorTable.updateToLastScan(librelink.getLibreMessage());
            rawScanTable.addLastSensorScan(librelink.getLibreMessage());
            realTimeReadingTable.addLastSensorScan(librelink.getLibreMessage());
            historicReadingTable.addLastSensorScan(librelink.getLibreMessage());
        });
    }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        performAction(() -> sensorTable.setFakeSerialNumberForLastSensor());
    }

    public void endLastSensor() throws Exception {
        performAction(() -> sensorTable.endLastSensor());
    }

    private void open() {
        this.db = SQLiteDatabase.openDatabase(context.getDatabasePath(SAS_DB).getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

        historicReadingTable = new HistoricReadingTable(this);
        rawScanTable = new RawScanTable(this);
        realTimeReadingTable = new RealTimeReadingTable(this);
        sensorSelectionRangeTable = new SensorSelectionRangeTable(this);
        sensorTable = new SensorTable(this);
        userTable = new UserTable(this);
    }
    private void performAction(ThrowingRunnable action) throws Exception {
        try {
            this.open();
            db.beginTransaction();
            action.run();
            db.setTransactionSuccessful();
        }
        catch (Throwable e){
            Logger.error(e);
            throw new Exception(e);
        }
        finally {
            db.endTransaction();
            db.close();
        }
    }

    public SQLiteDatabase getSQLite(){
        return db;
    }

    public LibreMessage getLibreMessage(){
        return librelink.getLibreMessage();
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

    public SensorSelectionRangeTable getSensorSelectionRangeTable(){
        return sensorSelectionRangeTable;
    }

    public SensorTable getSensorTable(){
        return sensorTable;
    }

    public UserTable getUserTable(){
        return userTable;
    }

    public void createDatabasesInOurApp() throws Exception {
        // TODO: проверить это
        try (SQLiteDatabase apollo = SQLiteDatabase.openOrCreateDatabase(App.getInstance().getApplicationContext().getDatabasePath(APOLLO_DB).getAbsolutePath(), null)) {

            apollo.beginTransaction();

            // таблицы

            apollo.execSQL("CREATE TABLE `alarms` (`alarmTimeLocal` BIGINT NOT NULL , `enabled` SMALLINT , `id` INTEGER PRIMARY KEY AUTOINCREMENT , `repeatFriday` SMALLINT , `repeatMonday` SMALLINT , `repeatSaturday` SMALLINT , `repeatSunday` SMALLINT , `repeatThursday` SMALLINT , `repeatTuesday` SMALLINT , `repeatWednesday` SMALLINT , `expireTimeUTC` BIGINT , `label` VARCHAR )");
            apollo.execSQL("CREATE TABLE `alarms_setting` (`highGlucoseAlarmEnabled` SMALLINT , `highGlucoseThresholdMgdl` INTEGER , `Id` INTEGER PRIMARY KEY AUTOINCREMENT , `isAlarmNotificationEnabled` SMALLINT , `lowGlucoseAlarmEnabled` SMALLINT , `lowGlucoseThresholdMgdl` INTEGER , `signalLossAlarmEnabled` SMALLINT , `timeZoneLocal` VARCHAR , `timestampUTC` BIGINT )");
            apollo.execSQL("CREATE TABLE `alarms_state` (`Id` INTEGER PRIMARY KEY AUTOINCREMENT , `isAlarmStateUpdated` SMALLINT , `isFixLowAlarmCleared` SMALLINT , `isFixLowAlarmDismissed` SMALLINT , `isFixLowAlarmPresented` SMALLINT , `isFixLowAlarmUserCleared` SMALLINT , `isFixLowEpisodeOngoing` SMALLINT , `isHighAlarmCleared` SMALLINT , `isHighAlarmDismissed` SMALLINT , `isHighAlarmPresented` SMALLINT , `isHighAlarmUserCleared` SMALLINT , `isHighEpisodeOngoing` SMALLINT , `isLowAlarmCleared` SMALLINT , `isLowAlarmDismissed` SMALLINT , `isLowAlarmPresented` SMALLINT , `isLowAlarmUserCleared` SMALLINT , `isLowEpisodeOngoing` SMALLINT , `isSignalLossAlarmAutoDismissed` SMALLINT , `isSignalLossAlarmCleared` SMALLINT , `isSignalLossAlarmPresented` SMALLINT , `isSignalLossAlarmUserCleared` SMALLINT , `isSignalLossAlarmUserDismissed` SMALLINT , `isSignalLossEpisodeOngoing` SMALLINT , `timeZoneLocal` VARCHAR , `timestampUTC` BIGINT )");
            //apollo.execSQL("CREATE TABLE android_metadata (locale TEXT)");
            apollo.execSQL("CREATE TABLE `appError` (`dateTime` BIGINT NOT NULL , `errorCode` INTEGER NOT NULL , `id` INTEGER PRIMARY KEY AUTOINCREMENT )");
            apollo.execSQL("CREATE TABLE `deletedNotes` (`Id` INTEGER PRIMARY KEY AUTOINCREMENT , `note_id` integer REFERENCES 'notes'('Id') ON DELETE CASCADE )");
            apollo.execSQL("CREATE TABLE `doses` (`Id` INTEGER PRIMARY KEY AUTOINCREMENT , `doseId` BIGINT , `doseScanTimestamp` VARCHAR , `units` INTEGER , `edited` SMALLINT , `elapsedSeconds` BIGINT , `insulinBrand` VARCHAR , `insulinType` VARCHAR , `invalid` SMALLINT , `newDose` SMALLINT , `pen` INTEGER NOT NULL , `errorCode` VARCHAR , `penName` VARCHAR , `prime` SMALLINT , `primeAlgo` SMALLINT , `time` BIGINT , `status` INTEGER , `timestamp` VARCHAR , `timestampUTC` VARCHAR , ` amount` FLOAT , `crc` BIGINT NOT NULL )");
            apollo.execSQL("CREATE TABLE `manualBgReadings` (`manualBgId` INTEGER PRIMARY KEY AUTOINCREMENT , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUtc` BIGINT NOT NULL , `value` INTEGER NOT NULL )");
            apollo.execSQL("CREATE TABLE `notes` (`carbUnit` INTEGER , `comment` VARCHAR , `deleted` SMALLINT , `doseType` VARCHAR , `exerciseIntensity` VARCHAR , `exerciseMinutes` INTEGER , `fastInsulinDose` `DOUBLE PRECISION` , `foodCarbs` `DOUBLE PRECISION` , `foodType` VARCHAR , `isAssociatedToScan` SMALLINT , `Id` INTEGER PRIMARY KEY AUTOINCREMENT , `penDose` INTEGER , `servingSize` FLOAT , `slowInsulinDose` `DOUBLE PRECISION` , `timeZoneLocal` VARCHAR , `timestampUTC` BIGINT )");
            apollo.execSQL("CREATE TABLE `pens` (`activated` SMALLINT , `timestampUTC` BIGINT , `errorCode` VARCHAR , `firmware` VARCHAR , `first_dose` INTEGER , `insulin_brand` VARCHAR , `insulin_type` VARCHAR , `last_dose` INTEGER , `last_scan_timestampUTC` BIGINT , `machine` VARCHAR , `manufacturer` VARCHAR , `model` VARCHAR , `localModel` VARCHAR , `name` VARCHAR , `color` VARCHAR , `Id` INTEGER PRIMARY KEY AUTOINCREMENT , `serial` VARCHAR NOT NULL , ` specification` VARCHAR , `uid` BIGINT NOT NULL , `crc` BIGINT NOT NULL ,  UNIQUE (`serial`),  UNIQUE (`uid`))");
            //apollo.execSQL("CREATE TABLE sqlite_sequence(name,seq)");
            apollo.execSQL("CREATE TABLE `timers` (`durationMillis` BIGINT , `enabled` SMALLINT , `id` INTEGER PRIMARY KEY AUTOINCREMENT , `startTimeUTC` BIGINT , `type` VARCHAR NOT NULL , `expireTimeUTC` BIGINT , `label` VARCHAR )");
            apollo.execSQL("CREATE TABLE `uploadRecords` (`forceUpload` SMALLINT , `id` INTEGER PRIMARY KEY AUTOINCREMENT , `maxAlarmsSettingId` INTEGER , `maxAlarmsStateId` INTEGER , `maxAppErrorId` INTEGER , `maxDeletedNoteId` INTEGER , `maxHistoricId` INTEGER , `maxManualBgId` INTEGER , `maxNoteId` INTEGER , `maxRealtimeReadingId` INTEGER , `maxSensorId` INTEGER )");

            // Индексы

            apollo.execSQL("CREATE INDEX `doses_pen_idx` ON `doses` ( `pen` )");
            apollo.execSQL("CREATE INDEX `manualBgReadings_manualBgId_idx` ON `manualBgReadings` ( `manualBgId` )");
            apollo.execSQL("CREATE INDEX `manualBgReadings_timestampLocal_idx` ON `manualBgReadings` ( `timestampLocal` )");
            apollo.execSQL("CREATE INDEX `pens_serial_idx` ON `pens` ( `serial` )");

            // добавляем запись в timers
            apollo.execSQL("INSERT INTO timers (durationMillis, enabled, startTimeUTC, type, expireTimeUTC, label) VALUES (28800000, 0, NULL, 'AUTOMATIC', NULL, 'Sensor scanning');");

            // ставим номер версии
            apollo.setVersion(11);

            if(SqlUtils.countTables(apollo) == 13
                    && apollo.getVersion() == 11
                    && SqlUtils.countIndexes(apollo) == 4 + 2){
                apollo.setTransactionSuccessful();
                apollo.endTransaction();
                Logger.ok("apollo.db created");
            }
            else {
                apollo.endTransaction();
                throw new Exception("Can not create correct apollo table.");
            }
        }

        try (SQLiteDatabase sas = SQLiteDatabase.openOrCreateDatabase(App.getInstance().getApplicationContext().getDatabasePath(SAS_DB).getAbsolutePath(), null)) {

            sas.beginTransaction();

            // таблицы

            //sas.execSQL("CREATE TABLE android_metadata (locale TEXT)");
            sas.execSQL("CREATE TABLE `currentErrors` (`dataQuality` INTEGER NOT NULL , `errorId` INTEGER PRIMARY KEY AUTOINCREMENT , `sensorId` INTEGER NOT NULL , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `currentReadings` (`glucoseValue` `DOUBLE PRECISION` NOT NULL , `rateOfChange` `DOUBLE PRECISION` NOT NULL , `readingId` INTEGER PRIMARY KEY AUTOINCREMENT , `sensorId` INTEGER NOT NULL , `timeChangeBefore` BIGINT NOT NULL , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `historicErrors` (`dataQuality` INTEGER NOT NULL , `errorId` INTEGER PRIMARY KEY AUTOINCREMENT , `sampleNumber` INTEGER NOT NULL , `sensorId` INTEGER NOT NULL , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `historicReadings` (`glucoseValue` `DOUBLE PRECISION` NOT NULL , `readingId` INTEGER PRIMARY KEY AUTOINCREMENT , `sampleNumber` INTEGER NOT NULL , `sensorId` INTEGER NOT NULL , `timeChangeBefore` BIGINT NOT NULL , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `rawScans` (`patchInfo` BLOB NOT NULL , `payload` BLOB NOT NULL , `scanId` INTEGER PRIMARY KEY AUTOINCREMENT , `sensorId` INTEGER NOT NULL , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `rawStreamings` (`payload` BLOB NOT NULL , `sensorId` INTEGER NOT NULL , `streamingId` INTEGER PRIMARY KEY AUTOINCREMENT , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `realTimeErrors` (`dataQuality` INTEGER NOT NULL , `errorId` INTEGER PRIMARY KEY AUTOINCREMENT , `sensorId` INTEGER NOT NULL , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `realTimeReadings` (`alarm` INTEGER NOT NULL , `glucoseValue` `DOUBLE PRECISION` NOT NULL , `isActionable` SMALLINT NOT NULL , `rateOfChange` `DOUBLE PRECISION` NOT NULL , `readingId` INTEGER PRIMARY KEY AUTOINCREMENT , `sensorId` INTEGER NOT NULL , `timeChangeBefore` BIGINT NOT NULL , `timeZone` VARCHAR NOT NULL , `timestampLocal` BIGINT NOT NULL , `timestampUTC` BIGINT NOT NULL , `trendArrow` INTEGER NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `sensorSelectionRanges` (`endTimestampUTC` BIGINT NOT NULL , `rangeId` INTEGER PRIMARY KEY AUTOINCREMENT , `sensorId` INTEGER NOT NULL , `startTimestampUTC` BIGINT NOT NULL , `CRC` BIGINT NOT NULL )");
            sas.execSQL("CREATE TABLE `sensors` (`attenuationState` BLOB , `bleAddress` BLOB , `compositeState` BLOB , `enableStreamingTimestamp` INTEGER NOT NULL , `endedEarly` SMALLINT NOT NULL , `initialPatchInformation` BLOB NOT NULL , `lastScanSampleNumber` INTEGER NOT NULL , `lastScanTimeZone` VARCHAR NOT NULL , `lastScanTimestampLocal` BIGINT NOT NULL , `lastScanTimestampUTC` BIGINT NOT NULL , `lsaDetected` SMALLINT NOT NULL , `measurementState` BLOB , `personalizationIndex` INTEGER NOT NULL , `sensorId` INTEGER PRIMARY KEY AUTOINCREMENT , `sensorStartTimeZone` VARCHAR NOT NULL , `sensorStartTimestampLocal` BIGINT NOT NULL , `sensorStartTimestampUTC` BIGINT NOT NULL , `serialNumber` VARCHAR NOT NULL , `streamingAuthenticationData` BLOB , `streamingUnlockCount` INTEGER NOT NULL , `uniqueIdentifier` BLOB NOT NULL , `unrecordedHistoricTimeChange` BIGINT NOT NULL , `unrecordedRealTimeTimeChange` BIGINT NOT NULL , `userId` INTEGER NOT NULL , `warmupPeriodInMinutes` INTEGER NOT NULL , `wearDurationInMinutes` INTEGER NOT NULL , `CRC` BIGINT NOT NULL ,  UNIQUE (`serialNumber`))");
            //sas.execSQL("CREATE TABLE sqlite_sequence(name,seq)");
            sas.execSQL("CREATE TABLE `users` (`name` VARCHAR NOT NULL , `userId` INTEGER PRIMARY KEY AUTOINCREMENT , `CRC` BIGINT NOT NULL ,  UNIQUE (`name`))");

            // индексы

            sas.execSQL("CREATE INDEX `currentErrors_sensorId_idx` ON `currentErrors` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `currentErrors_timestampLocal_idx` ON `currentErrors` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `currentErrors_timestampUTC_idx` ON `currentErrors` ( `timestampUTC` )");
            sas.execSQL("CREATE INDEX `currentReadings_sensorId_idx` ON `currentReadings` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `currentReadings_timestampLocal_idx` ON `currentReadings` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `currentReadings_timestampUTC_idx` ON `currentReadings` ( `timestampUTC` )");
            sas.execSQL("CREATE INDEX `historicErrors_sensorId_idx` ON `historicErrors` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `historicErrors_timestampLocal_idx` ON `historicErrors` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `historicErrors_timestampUTC_idx` ON `historicErrors` ( `timestampUTC` )");
            sas.execSQL("CREATE INDEX `historicReadings_sensorId_idx` ON `historicReadings` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `historicReadings_timestampLocal_idx` ON `historicReadings` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `historicReadings_timestampUTC_idx` ON `historicReadings` ( `timestampUTC` )");
            sas.execSQL("CREATE INDEX `rawScans_sensorId_idx` ON `rawScans` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `rawScans_timestampLocal_idx` ON `rawScans` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `rawScans_timestampUTC_idx` ON `rawScans` ( `timestampUTC` )");
            sas.execSQL("CREATE INDEX `rawStreamings_sensorId_idx` ON `rawStreamings` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `rawStreamings_timestampLocal_idx` ON `rawStreamings` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `rawStreamings_timestampUTC_idx` ON `rawStreamings` ( `timestampUTC` )");
            sas.execSQL("CREATE INDEX `realTimeErrors_sensorId_idx` ON `realTimeErrors` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `realTimeErrors_timestampLocal_idx` ON `realTimeErrors` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `realTimeErrors_timestampUTC_idx` ON `realTimeErrors` ( `timestampUTC` )");
            sas.execSQL("CREATE INDEX `realTimeReadings_sensorId_idx` ON `realTimeReadings` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `realTimeReadings_timestampLocal_idx` ON `realTimeReadings` ( `timestampLocal` )");
            sas.execSQL("CREATE INDEX `realTimeReadings_timestampUTC_idx` ON `realTimeReadings` ( `timestampUTC` )");
            sas.execSQL("CREATE UNIQUE INDEX `sensorSampleNumberError` ON `historicErrors` ( `sampleNumber`, `sensorId` )");
            sas.execSQL("CREATE UNIQUE INDEX `sensorSampleNumberReading` ON `historicReadings` ( `sampleNumber`, `sensorId` )");
            sas.execSQL("CREATE INDEX `sensorSelectionRanges_endTimestampUTC_idx` ON `sensorSelectionRanges` ( `endTimestampUTC` )");
            sas.execSQL("CREATE INDEX `sensorSelectionRanges_sensorId_idx` ON `sensorSelectionRanges` ( `sensorId` )");
            sas.execSQL("CREATE INDEX `sensors_sensorStartTimestampUTC_idx` ON `sensors` ( `sensorStartTimestampUTC` )");
            sas.execSQL("CREATE INDEX `sensors_serialNumber_idx` ON `sensors` ( `serialNumber` )");
            sas.execSQL("CREATE INDEX `users_name_idx` ON `users` ( `name` )");

            // добавляем запись в users
            sas.execSQL("INSERT INTO users (name, CRC) VALUES ('user', 3902082614);");

            // ставим номер версии
            sas.setVersion(8);

            if(SqlUtils.countTables(sas) == 13
                    && sas.getVersion() == 8
                    && SqlUtils.countIndexes(sas) == 31 + 2){
                sas.setTransactionSuccessful();
                sas.endTransaction();
                Logger.ok("sas.db created");
            }
            else {
                sas.endTransaction();
                throw new Exception("Can not create correct sas table.");
            }
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
