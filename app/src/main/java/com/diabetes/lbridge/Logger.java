package com.diabetes.lbridge;

import static com.diabetes.lbridge.App.TAG;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    private final AppDatabase appDatabase;

    private Logger() throws Exception {
        this.appDatabase = App.getInstance().getAppDatabase();
    }

    private static final Logger instance;

    static {
        try {
            instance = new Logger();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final List<LogListener> logListeners = new ArrayList<>();
    private static final DateTimeFormatter dbTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    public static void setLoggerListener(LogListener listener) {
        logListeners.add(listener);
    }

    public static void warn(String log){
        Log.w(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "WARN";

        logListeners.forEach(l -> l.logReceived(new LogRecord(Logger.getLogRecordCount(), timeUTC, status, log)));

        instance.writeToDatabase(timeUTC, status, log);
    }

    public static void inf(String log) {
        Log.i(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "INFO";

        logListeners.forEach(l -> l.logReceived(new LogRecord(Logger.getLogRecordCount(), timeUTC, status, log)));

        instance.writeToDatabase(timeUTC, status, log);
    }

    public static void ok(String log) {
        Log.i(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "OK";

        logListeners.forEach(l -> l.logReceived(new LogRecord(Logger.getLogRecordCount(), timeUTC, status, log)));

        instance.writeToDatabase(timeUTC, status, log);
    }

    public static void error(String log) {
        Log.e(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "ERROR";

        logListeners.forEach(l -> l.logReceived(new LogRecord(Logger.getLogRecordCount(), timeUTC, status, log)));

        instance.writeToDatabase(timeUTC, status, log);
    }

    public static void criticalError(String log) {
        Log.e(TAG, log);

        final LocalDateTime timeUTC = LocalDateTime.now(ZoneOffset.UTC);
        final String status = "CRITICAL_ERROR";

        logListeners.forEach(l -> l.logReceived(new LogRecord(Logger.getLogRecordCount(), timeUTC, status, log)));

        instance.writeToDatabase(timeUTC, status, log);
    }

    public static void criticalError(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw); // to PrintWriter
        // to console and logger windows
        Logger.criticalError(sw.toString());
    }

    public static void error(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw); // to PrintWriter
        // to console and logger windows
        Logger.error(sw.toString());
    }

    private void writeToDatabase(LocalDateTime utc, String status, String message) {
        try {
            App.getInstance().getAppDatabase().execInTransaction(() -> {
                ContentValues values = new ContentValues();
                values.put(TableStrings.dateTimeUTC, utc.format(dbTimeFormatter));
                values.put(TableStrings.status, status);
                values.put(TableStrings.message, message);
                appDatabase.getSQLite().insertOrThrow(TableStrings.TABLE_NAME, null, values);
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static LogRecord[] getLogs(int minId, int maxId) {
        Cursor cursor = null;
        List<LogRecord> logsList = new ArrayList<>();

        try {
            String sql = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s BETWEEN %s AND %s",
                    TableStrings.id, TableStrings.dateTimeUTC,
                    TableStrings.status, TableStrings.message,
                    TableStrings.TABLE_NAME,
                    TableStrings.id, minId, maxId);
            cursor = instance.appDatabase.getSQLite().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(TableStrings.id);
                int statusIndex = cursor.getColumnIndex(TableStrings.status);
                int timeIndex = cursor.getColumnIndex(TableStrings.dateTimeUTC);
                int messageIndex = cursor.getColumnIndex(TableStrings.message);

                if (idIndex >= 0 && statusIndex >= 0 && timeIndex >= 0 && messageIndex >= 0) {
                    int id = cursor.getInt(idIndex);
                    String status = cursor.getString(statusIndex);
                    LocalDateTime time = LocalDateTime.parse(cursor.getString(timeIndex), dbTimeFormatter);
                    String message = cursor.getString(messageIndex);
                    LogRecord logRecord = new LogRecord(id, time, status, message);
                    logsList.add(logRecord);
                } else {
                    Logger.inf("Column no found in database");
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

    public static int getLogRecordCount() {
        Cursor cursor = instance.appDatabase.getSQLite().rawQuery(String.format("SELECT COUNT(*) FROM %s", TableStrings.TABLE_NAME), null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public static void recreateLogTable() throws Exception {
        instance.appDatabase.recreateLogTable();
    }

    static class LogRecord {
        private final int id;
        private final LocalDateTime dateTimeUTC;
        private final String status;
        private final String message;

        LogRecord(int id, LocalDateTime dateTimeUTC, String status, String message) {
            this.id = id;
            this.dateTimeUTC = dateTimeUTC;
            this.status = status;
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getDateTimeLocal() {
            return Utils.fromUtcToLocal(dateTimeUTC);
        }

        public LocalDateTime getDateTimeUTC() {
            return dateTimeUTC;
        }

        public String toShortString() {
            String[] messageLines = this.getMessage().split("\n");

            String shortString = String.format("[%s] [%s] %s", this.getDateTimeLocal().format(DateTimeFormatter.ofPattern("HH:mm")),
                    this.getStatus(), messageLines[0]);

            if(messageLines.length > 1) { shortString += String.format(" ... %s lines more.", messageLines.length - 1); }
            return shortString;
        }

        public String toFullString(){
            return String.format("[%s] [%s] %s", this.getDateTimeLocal().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    this.getStatus(), this.getMessage());
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
