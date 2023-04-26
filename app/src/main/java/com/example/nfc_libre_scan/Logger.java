package com.example.nfc_libre_scan;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    private final SQLiteDatabase db;

    private Logger() {
        this.db = SQLiteDatabase.openOrCreateDatabase(App.getInstance().getApplicationContext().getDatabasePath("logs.db").getAbsolutePath(), null);
        this.createLogTable();
    }

    private static final Logger instance = new Logger();
    private static LogListener logListener;
    private static final String TAG = App.getInstance().getApplicationContext().getString(R.string.app_name);
    private static final DateTimeFormatter dbTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    public static void setLoggerListener(LogListener listener) {
        logListener = listener;
    }


    public static void inf(String log) {
        Log.i(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "INFO";

        instance.writeToDatabase(timeUTC, status, log);
        if (logListener != null) {
            logListener.logReceived(new LogRecord(timeUTC, status, log));
        }
    }

    public static void ok(String log) {
        Log.i(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "OK";

        instance.writeToDatabase(timeUTC, status, log);
        if (logListener != null) {
            logListener.logReceived(new LogRecord(timeUTC, status, log));
        }
    }

    public static void error(String log) {
        Log.e(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "ERROR";

        instance.writeToDatabase(timeUTC, status, log);
        if (logListener != null) {
            logListener.logReceived(new LogRecord(timeUTC, status, log));
        }
    }

    public static void criticalError(String log) {
        Log.e(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "CRITICAL_ERROR";

        instance.writeToDatabase(timeUTC, status, log);
        if (logListener != null) {
            logListener.logReceived(new LogRecord(timeUTC, status, log));
        }
    }

    public static void criticalError(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw); // to PrintWriter
        e.printStackTrace(); // to console
        Logger.criticalError(sw.toString()); // to main activity logger window
    }

    public static void error(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw); // to PrintWriter
        e.printStackTrace(); // to console
        Logger.error(sw.toString()); // to main activity logger window
    }

    private void writeToDatabase(LocalDateTime time, String status, String message) {
        try {
            ContentValues values = new ContentValues();
            values.put(TableStrings.dateTimeUTC, time.format(dbTimeFormatter));
            values.put(TableStrings.status, status);
            values.put(TableStrings.message, message);
            db.insertOrThrow(TableStrings.TABLE_NAME, null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public static LogRecord[] getLogs() {
        String[] columns = {TableStrings.id, TableStrings.dateTimeUTC, TableStrings.status, TableStrings.message};
        Cursor cursor = null;
        List<LogRecord> logsList = new ArrayList<>();

        try {
            cursor = instance.db.query(TableStrings.TABLE_NAME, columns, null, null, null, null, null);
            while (cursor.moveToNext()) {
                int statusIndex = cursor.getColumnIndex(TableStrings.status);
                int timeIndex = cursor.getColumnIndex(TableStrings.dateTimeUTC);
                int messageIndex = cursor.getColumnIndex(TableStrings.message);

                if (statusIndex >= 0 && timeIndex >= 0 && messageIndex >= 0) {
                    String status = cursor.getString(statusIndex);
                    LocalDateTime time = LocalDateTime.parse(cursor.getString(timeIndex), dbTimeFormatter);
                    String message = cursor.getString(messageIndex);
                    LogRecord logRecord = new LogRecord(time, status, message);
                    logsList.add(logRecord);
                } else {
                    Logger.error("Column no found in database");
                }
            }
        } catch (SQLiteException e) {
            Logger.error(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return logsList.toArray(new LogRecord[0]);
    }

    public static void recreateLogDb() {
        instance.recreateLogTable();
    }

    private void createLogTable() {
        this.db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s DATETIME, %s TEXT, %s TEXT);)",
                TableStrings.TABLE_NAME, TableStrings.id, TableStrings.dateTimeUTC, TableStrings.status, TableStrings.message));
    }

    private void recreateLogTable() {
        this.db.execSQL(String.format("DROP TABLE IF EXISTS %s", TableStrings.TABLE_NAME));
        this.createLogTable();
    }

    static class LogRecord {
        private final LocalDateTime dateTimeUTC;
        private final String status;
        private final String message;

        LogRecord(LocalDateTime dateTimeUTC, String status, String message) {
            this.dateTimeUTC = dateTimeUTC;
            this.status = status;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getDateTimeLocal() {
            return dateTimeUTC.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }

        public LocalDateTime getDateTimeUTC() {
            return dateTimeUTC;
        }

        public String toShortString() {
            return String.format("[%s] [%s] %s", this.getDateTimeLocal().format(DateTimeFormatter.ofPattern("HH:mm")), this.getStatus(), this.getMessage());
        }
    }


    private static class TableStrings {
        final static String TABLE_NAME = "logs";
        final static String id = "Id";
        final static String status = "Status";
        final static String dateTimeUTC = "DateTimeUTC";
        final static String message = "Message";
    }
}
