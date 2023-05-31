package com.diabetes.lbridge.librelink.rows;

import android.content.ContentValues;
import android.database.Cursor;

import com.diabetes.lbridge.librelink.tables.RealTimeReadingTable;
import com.diabetes.lbridge.libre.LibreMessage;
import com.oop1.GlucoseUnit;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.zip.CRC32;

public class RealTimeReadingRow implements Row, TimeRow, ScanTimeRow, CrcRow {
    private final RealTimeReadingTable table;

    public RealTimeReadingRow(final RealTimeReadingTable table,
                              final int rowIndex){
        this.table = table;
        String query = Row.getBaseRowSearchingSQL(table);
        Cursor cursor = table.getDatabase().getSQLite().rawQuery(query, null);
        cursor.moveToPosition(rowIndex); // перемещаемся на строку с индексом rowIndex

        this.alarm = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.alarm));
        this.glucoseValue = cursor.getDouble(cursor.getColumnIndexOrThrow(RowColumns.glucoseValue));
        this.isActionable = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.isActionable)) != 0;
        this.rateOfChange = cursor.getDouble(cursor.getColumnIndexOrThrow(RowColumns.timeChangeBefore));
        this.readingId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.readingId));
        this.sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.sensorId));
        this.timeChangeBefore = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timeChangeBefore));
        this.timeZone = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.timeZone));
        this.timestampLocal = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timestampLocal));
        this.timestampUTC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timestampUTC));
        this.trendArrow = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.trendArrow));
        this.CRC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.CRC));

        cursor.close();
    }


    public RealTimeReadingRow(final RealTimeReadingTable table,
                              final LibreMessage libreMessage,
                              final double glucoseValue,
                              final boolean isActionable,
                              final double rateOfChange,
                              final int sensorId,
                              final long timeChangeBefore,
                              final String timeZone,
                              final long timestampLocal,
                              final long timestampUTC,
                              final int trendArrow) {
        this.table = table;

        this.alarm = this.computeAlarm(libreMessage);
        this.glucoseValue = glucoseValue;
        this.isActionable = isActionable;
        this.rateOfChange = rateOfChange;
        this.readingId = table.getLastStoredReadingId() + 1;
        this.sensorId = sensorId;
        this.timeChangeBefore = timeChangeBefore;
        this.timeZone = timeZone;
        this.timestampLocal = timestampLocal;
        this.timestampUTC = timestampUTC;
        this.trendArrow = trendArrow;
    }

    @Override
    public void insertOrThrow() throws Exception {
        ContentValues values = new ContentValues();
        values.put(RowColumns.alarm, alarm);
        values.put(RowColumns.glucoseValue, glucoseValue);
        values.put(RowColumns.isActionable, isActionable);
        values.put(RowColumns.rateOfChange, rateOfChange);
        // не нужно менять readingId, так как это значение само увеличивается при добавлении записи.
        //values.put(TableStrings.readingId, readingId);
        values.put(RowColumns.sensorId, sensorId);
        values.put(RowColumns.timeChangeBefore, timeChangeBefore);
        values.put(RowColumns.timeZone, timeZone);
        values.put(RowColumns.timestampUTC, timestampUTC);
        values.put(RowColumns.timestampLocal, timestampLocal);
        values.put(RowColumns.trendArrow, trendArrow);
        values.put(RowColumns.CRC, this.computeCRC32());

        table.getDatabase().getSQLite().insertOrThrow(table.getName(), null, values);
        table.rowInserted();
    }

    private int computeAlarm(LibreMessage libreMessage) {
        /*
         * NOT_DETERMINED(0),
         * LOW_GLUCOSE(1),
         * PROJECTED_LOW_GLUCOSE(2),
         * GLUCOSE_OK(3),
         * PROJECTED_HIGH_GLUCOSE(4),
         * HIGH_GLUCOSE(5);
         */
        double bg = libreMessage.getCurrentBg().convertBG(GlucoseUnit.MMOL).getBG();
        if (bg < 3.9) {
            return 1;
        } else if (bg > 10.0) {
            return 5;
        } else {
            return 3;
        }
    }

    public long computeCRC32() throws Exception {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.sensorId);
        dataOutputStream.writeLong(this.timestampUTC);
        dataOutputStream.writeLong(this.timestampLocal);
        dataOutputStream.writeUTF(this.timeZone);
        dataOutputStream.writeDouble(this.glucoseValue);
        dataOutputStream.writeDouble(this.rateOfChange);
        dataOutputStream.writeInt(this.trendArrow);
        dataOutputStream.writeInt(this.alarm);
        dataOutputStream.writeBoolean(this.isActionable);
        dataOutputStream.writeLong(this.timeChangeBefore);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    public int getReadingId(){
        return readingId;
    }

    private final int alarm;
    private final double glucoseValue;
    private final boolean isActionable;
    private final double rateOfChange;
    private final int readingId;
    private final int sensorId;
    private final long timeChangeBefore;
    private final String timeZone;
    private final long timestampLocal;
    private final long timestampUTC;
    private final int trendArrow;
    private long CRC;

    @Override
    public long getBiggestTimestampUTC() {
        return this.timestampUTC;
    }

    @Override
    public long getScanTimestampUTC() {
        return this.timestampUTC;
    }

    @Override
    public void validateCRC() throws Exception {
        if(this.CRC != this.computeCRC32()){
            throw new Exception("CRC is not valid.");
        }
    }

    private static class RowColumns {
        final static String alarm = "alarm";
        final static String glucoseValue = "glucoseValue";
        final static String isActionable = "isActionable";
        final static String rateOfChange = "rateOfChange";
        final static String readingId = "readingId";
        final static String sensorId = "sensorId";
        final static String timeChangeBefore = "timeChangeBefore";
        final static String timeZone = "timeZone";
        final static String timestampLocal = "timestampLocal";
        final static String timestampUTC = "timestampUTC";
        final static String trendArrow = "trendArrow";
        final static String CRC = "CRC";
    }
}
