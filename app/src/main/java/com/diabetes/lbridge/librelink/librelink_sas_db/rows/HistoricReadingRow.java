package com.diabetes.lbridge.librelink.librelink_sas_db.rows;

import android.content.ContentValues;
import android.database.Cursor;

import com.diabetes.lbridge.librelink.librelink_sas_db.tables.HistoricReadingTable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.zip.CRC32;

public class HistoricReadingRow implements Row, TimeRow, ScanTimeRow, CrcRow {

    private final HistoricReadingTable table;

    public HistoricReadingRow(final HistoricReadingTable table, int rowIndex) {
        this.table = table;

        String query = Row.getBaseRowSearchingSQL(table);
        Cursor cursor = table.getDatabase().getSQLite().rawQuery(query, null);
        cursor.moveToPosition(rowIndex); // перемещаемся на строку с индексом rowIndex

        this.glucoseValue = cursor.getDouble(cursor.getColumnIndexOrThrow(RowColumns.glucoseValue));
        this.sampleNumber = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.sampleNumber));
        this.readingId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.readingId));
        this.sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.sensorId));
        this.timeChangeBefore = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timeChangeBefore));
        this.timeZone = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.timeZone));
        this.timestampLocal = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timestampLocal));
        this.timestampUTC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timestampUTC));
        this.CRC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.CRC));

        cursor.close();
    }


    public HistoricReadingRow(final HistoricReadingTable table,
                              final double glucoseValue,
                              final int sampleNumber,
                              final int sensorId,
                              final long timeChangeBefore,
                              final String timeZone,
                              final long timestampLocal,
                              final long timestampUTC
    ) {
        this.table = table;

        this.glucoseValue = glucoseValue;
        this.readingId = table.getLastStoredReadingId() + 1;
        this.sampleNumber = sampleNumber;
        this.sensorId = sensorId;
        this.timeChangeBefore = timeChangeBefore;
        this.timeZone = timeZone;
        this.timestampLocal = timestampLocal;
        this.timestampUTC = timestampUTC;
    }

    @Override
    public void insertOrThrow() throws Exception {

        ContentValues values = new ContentValues();
        values.put(RowColumns.glucoseValue, glucoseValue);
        // не нужно менять readingId, так как это значение само увеличивается при добавлении записи.
        values.put(RowColumns.sampleNumber, this.sampleNumber);
        values.put(RowColumns.sensorId, sensorId);
        values.put(RowColumns.timeChangeBefore, timeChangeBefore);
        values.put(RowColumns.timeZone, timeZone);
        values.put(RowColumns.timestampUTC, timestampUTC);
        values.put(RowColumns.timestampLocal, timestampLocal);
        values.put(RowColumns.CRC, this.computeCRC32());

        table.getDatabase().getSQLite().insertOrThrow(table.getName(), null, values);
        table.rowInserted();
    }

    public long computeCRC32() throws Exception {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.sensorId);
        dataOutputStream.writeInt(this.sampleNumber);
        dataOutputStream.writeLong(this.timestampUTC);
        dataOutputStream.writeLong(this.timestampLocal);
        dataOutputStream.writeUTF(this.timeZone);
        dataOutputStream.writeDouble(this.glucoseValue);
        dataOutputStream.writeLong(this.timeChangeBefore);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    public int getSampleNumber() {
        return sampleNumber;
    }

    public int getReadingId() {
        return readingId;
    }

    private final double glucoseValue;
    private final int readingId;
    private final int sampleNumber;
    private final int sensorId;
    private final long timeChangeBefore;
    private final String timeZone;
    private final long timestampLocal;
    private final long timestampUTC;
    private long CRC;

    @Override
    public long getBiggestTimestampUTC() {
        return this.timestampUTC;
    }

    @Override
    public long getScanTimestampUTC() {
        return this.timestampUTC;
    }

    public int getSensorId(){ return this.sensorId; }

    @Override
    public void validateCRC() throws Exception {
        if(this.CRC != this.computeCRC32()){
            throw new Exception("CRC is not valid.");
        }
    }

    private static class RowColumns {
        final static String glucoseValue = "glucoseValue";
        final static String readingId = "readingId";
        final static String sampleNumber = "sampleNumber";
        final static String sensorId = "sensorId";
        final static String timeChangeBefore = "timeChangeBefore";
        final static String timeZone = "timeZone";
        final static String timestampLocal = "timestampLocal";
        final static String timestampUTC = "timestampUTC";
        final static String CRC = "CRC";
    }
}
