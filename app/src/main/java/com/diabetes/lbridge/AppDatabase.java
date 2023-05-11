package com.diabetes.lbridge;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class AppDatabase {
    private final SQLiteDatabase appDatabase;

    AppDatabase() throws Exception {
        Context appContext = App.getInstance().getApplicationContext();
        final String appDatabaseName = String.format("%s.db", App.getInstance().getString(R.string.app_name));
        this.appDatabase = SQLiteDatabase.openOrCreateDatabase(appContext.getDatabasePath(appDatabaseName).getAbsolutePath(), null);

        this.createOptionsTableIfNotExists();
        this.createSensorAliasTableIfNotExists();
        this.createLogTableIfNotExists();
    }

    public SQLiteDatabase getSQLite(){
        return appDatabase;
    }

    private void createLogTableIfNotExists() throws Exception {
        execInTransaction(() -> this.appDatabase.execSQL("CREATE TABLE IF NOT EXISTS logs (Id INTEGER PRIMARY KEY AUTOINCREMENT, DateTimeUTC DATETIME, Status TEXT, Message TEXT);"));
    }

    private void dropLogTable() throws Exception {
        execInTransaction(() -> this.appDatabase.execSQL("DROP TABLE IF EXISTS logs"));
    }

    public void recreateLogTable() throws Exception {
        this.dropLogTable();
        this.createLogTableIfNotExists();
    }

    private void createSensorAliasTableIfNotExists() throws Exception {
        execInTransaction(() -> this.appDatabase.execSQL("CREATE TABLE IF NOT EXISTS sensorAliases (originalLibreSN TEXT NOT NULL, fakeSN TEXT NOT NULL, UNIQUE (`originalLibreSN`), UNIQUE (`fakeSN`));"));
    }

    private void dropSensorAliasTable() throws Exception {
        execInTransaction(() -> this.appDatabase.execSQL("DROP TABLE IF EXISTS sensorAliases"));
    }

    public void recreateSensorAliasTable() throws Exception {
        this.dropSensorAliasTable();
        this.createSensorAliasTableIfNotExists();
    }

    private void createOptionsTableIfNotExists() throws Exception {
        execInTransaction(() -> this.appDatabase.execSQL("CREATE TABLE IF NOT EXISTS options (name TEXT NOT NULL, value TEXT NOT NULL, UNIQUE (`name`));"));
    }

    public void close(){
        appDatabase.close();
    }

    public void execInTransaction(ThrowingRunnable action) throws Exception {
        try {
            appDatabase.beginTransaction();
            action.run();
            appDatabase.setTransactionSuccessful();
        }
        catch (Throwable e){
            throw new Exception(e);
        }
        finally {
            appDatabase.endTransaction();
        }
    }
}
