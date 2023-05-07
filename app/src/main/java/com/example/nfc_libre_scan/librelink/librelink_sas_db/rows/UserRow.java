package com.example.nfc_libre_scan.librelink.librelink_sas_db.rows;

import android.database.Cursor;

import com.example.nfc_libre_scan.librelink.librelink_sas_db.Row;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.UserTable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class UserRow implements Row {
    private final UserTable table;

    public UserRow(final UserTable table,
                   final int rowIndex){
        this.table = table;
        String query = String.format("SELECT * FROM %s WHERE _rowid_=%s", table.getName(), rowIndex);
        Cursor cursor = table.getDatabase().getSQLite().rawQuery(query, null);

        this.name = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.name));
        this.userId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.userId));
        this.CRC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.CRC));
        cursor.close();
    }

    UserRow(final UserTable table,
            final String name,
            final int userId) throws IOException {
        this.table = table;
        this.name = name;
        this.userId = userId;
        this.CRC = this.computeCRC32();

    }

    public int getUserId(){
        return userId;
    }

    public long computeCRC32() throws IOException {
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
    private final long CRC;

    private static class RowColumns{
        final static String name = "name";
        final static String userId = "userId";
        final static String CRC = "CRC";
    }
}
