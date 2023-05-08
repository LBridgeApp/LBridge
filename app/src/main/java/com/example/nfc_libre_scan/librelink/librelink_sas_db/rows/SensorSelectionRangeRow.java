package com.example.nfc_libre_scan.librelink.librelink_sas_db.rows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.example.nfc_libre_scan.librelink.librelink_sas_db.Row;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.SensorSelectionRangeTable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class SensorSelectionRangeRow implements Row {
    private final SensorSelectionRangeTable table;
    private final List<SQLiteStatement> sqlChangingList = new ArrayList<>();

    public SensorSelectionRangeRow(final SensorSelectionRangeTable table,
                                   final int rowIndex) {
        this.table = table;
        String query = Row.getBaseRowSearchingSQL(table);
        Cursor cursor = table.getDatabase().getSQLite().rawQuery(query, null);
        cursor.moveToPosition(rowIndex); // перемещаемся на строку с индексом rowIndex

        this.endTimestampUTC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.endTimestampUTC));
        this.rangeId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.rangeId));
        this.sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.sensorId));
        this.startTimestampUTC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.startTimestampUTC));
        this.CRC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.CRC));

        cursor.close();
    }

    public SensorSelectionRangeRow(final SensorSelectionRangeTable table,
                                   final long endTimestampUTC,
                                   final int rangeId,
                                   final int sensorId,
                                   final long startTimestampUTC) {
        this.table = table;

        this.endTimestampUTC = endTimestampUTC;
        this.rangeId = rangeId;
        this.sensorId = sensorId;
        this.startTimestampUTC = startTimestampUTC;
    }

    @Override
    public void insertOrThrow() throws IOException {

        ContentValues values = new ContentValues();
        values.put(RowColumns.endTimestampUTC, endTimestampUTC);
        values.put(RowColumns.rangeId, rangeId);
        values.put(RowColumns.sensorId, sensorId);
        values.put(RowColumns.startTimestampUTC, startTimestampUTC);
        values.put(RowColumns.CRC, this.computeCRC32());

        table.getDatabase().getSQLite().insertOrThrow(table.getName(), null, values);
        table.rowInserted();
    }

    public void replace() throws IOException {
        this.setCRC(this.computeCRC32()); // setCRC добавляет SQL запрос на изменение CRC
        sqlChangingList.forEach(SQLiteStatement::execute);
        sqlChangingList.forEach(SQLiteStatement::close);
        sqlChangingList.clear();
    }

    public SensorSelectionRangeRow setEndTimestampUTC(long endTimestampUTC) {
        this.endTimestampUTC = endTimestampUTC;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(table, RowColumns.endTimestampUTC, RowColumns.sensorId, sensorId));
        statement.bindLong(1, endTimestampUTC);
        this.sqlChangingList.add(statement);
        return this;
    }

    private void setCRC(long CRC) {
        this.CRC = CRC;
        SQLiteStatement statement = table.getDatabase().getSQLite()
                .compileStatement(Row.getBaseUpdatingSQL(table, RowColumns.CRC, RowColumns.sensorId, sensorId));
        statement.bindLong(1, CRC);
        this.sqlChangingList.add(statement);
    }

    public long computeCRC32() throws IOException {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.sensorId);
        dataOutputStream.writeLong(this.startTimestampUTC);
        dataOutputStream.writeLong(this.endTimestampUTC);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }


    public int getRangeId() {
        return rangeId;
    }

    public int getSensorId() {
        return sensorId;
    }


    private long endTimestampUTC;
    private final int rangeId;
    private final int sensorId;
    private final long startTimestampUTC;
    private long CRC;

    private static class RowColumns {
        final static String endTimestampUTC = "endTimestampUTC";
        final static String rangeId = "rangeId";
        final static String sensorId = "sensorId";
        final static String startTimestampUTC = "startTimestampUTC";
        final static String CRC = "CRC";
    }
}