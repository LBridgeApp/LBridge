package com.diabetes.lbridge.librelink.rows;

import android.content.ContentValues;
import android.database.Cursor;

import com.diabetes.lbridge.librelink.tables.UserTable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import java.util.zip.CRC32;

public class UserRow implements Row, CrcRow {
    private final UserTable table;

    public UserRow(final UserTable table,
                   final int rowIndex) {
        this.table = table;
        String query = Row.getBaseRowSearchingSQL(table);
        Cursor cursor = table.getDatabase().getSQLite().rawQuery(query, null);
        cursor.moveToPosition(rowIndex); // перемещаемся на строку с индексом rowIndex

        this.name = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.name));
        this.userId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.userId));
        this.CRC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.CRC));

        cursor.close();
    }

    UserRow(final UserTable table,
            final String name,
            final int userId) {
        this.table = table;

        this.name = name;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public void insertOrThrow() throws Exception {
        ContentValues values = new ContentValues();
        values.put(RowColumns.name, name);
        // не нужно менять userId, так как это значение само увеличивается при добавлении записи.
        values.put(RowColumns.CRC, this.computeCRC32());

        table.getDatabase().getSQLite().insertOrThrow(table.getName(), null, values);
        table.rowInserted();
    }

    public long computeCRC32() throws Exception {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeUTF(this.name);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    private final String name;
    private final int userId;
    private long CRC;
    @Override
    public void validateCRC() throws Exception {
        if(this.CRC != this.computeCRC32()){
            throw new Exception("CRC is not valid.");
        }
    }

    private static class RowColumns {
    final static String name = "name";
    final static String userId = "userId";
    final static String CRC = "CRC";
}
}
