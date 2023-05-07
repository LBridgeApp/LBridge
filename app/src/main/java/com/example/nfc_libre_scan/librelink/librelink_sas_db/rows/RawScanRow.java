package com.example.nfc_libre_scan.librelink.librelink_sas_db.rows;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.nfc_libre_scan.librelink.librelink_sas_db.LibreLinkDatabase;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.RawScanTable;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.Row;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class RawScanRow implements Row {
    private final RawScanTable table;

    public RawScanRow(final RawScanTable table,
                      final int rowIndex){
        this.table = table;
        String query = String.format("SELECT * FROM %s WHERE _rowid_=%s", table.getName(), rowIndex);
        Cursor cursor = table.getDatabase().getSQLite().rawQuery(query, null);

        this.patchInfo = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.patchInfo));
        this.payload = cursor.getBlob(cursor.getColumnIndexOrThrow(RowColumns.payload));
        this.scanId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.scanId));
        this.sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(RowColumns.sensorId));
        this.timeZone = cursor.getString(cursor.getColumnIndexOrThrow(RowColumns.timeZone));
        this.timestampLocal = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timestampLocal));
        this.timestampUTC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.timestampUTC));
        this.CRC = cursor.getLong(cursor.getColumnIndexOrThrow(RowColumns.CRC));
        cursor.close();
    }

    public RawScanRow(final RawScanTable table,
                      final byte[] patchInfo,
                      final byte[] payload,
                      final int sensorId,
                      final String timeZone,
                      final long timestampLocal,
                      final long timestampUTC) throws IOException {
        this.table = table;
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.scanId = table.getLastStoredScanId() + 1;
        this.sensorId = sensorId;
        this.timeZone = timeZone;
        this.timestampLocal = timestampLocal;
        this.timestampUTC = timestampUTC;
        this.CRC = this.computeCRC32();
    }

    public void insertOrThrow() {

        ContentValues values = new ContentValues();
        values.put(RowColumns.patchInfo, patchInfo);
        values.put(RowColumns.payload, payload);
        // не нужно менять scanId, так как это значение само увеличивается при добавлении записи.
        values.put(RowColumns.sensorId, sensorId);
        values.put(RowColumns.timeZone, timeZone);
        values.put(RowColumns.timestampLocal, timestampLocal);
        values.put(RowColumns.timestampUTC, timestampUTC);
        values.put(RowColumns.CRC, CRC);

        table.getDatabase().getSQLite().insertOrThrow(table.getName(), null, values);
    }

    public long computeCRC32() throws IOException {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeInt(this.sensorId);
        dataOutputStream.writeLong(this.timestampUTC);
        dataOutputStream.writeLong(this.timestampLocal);
        dataOutputStream.writeUTF(this.timeZone);
        dataOutputStream.write(this.patchInfo);
        dataOutputStream.write(this.payload);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    public int getScanId(){ return scanId; }

    private final byte[] patchInfo;
    private final byte[] payload;
    private final int scanId;
    private final int sensorId;
    private final String timeZone;
    private final long timestampLocal;
    private final long timestampUTC;
    private final long CRC;

    private static class RowColumns {
        static final String patchInfo = "patchInfo";
        static final String payload = "payload";
        static final String scanId = "scanId";
        static final String sensorId = "sensorId";
        static final String timeZone = "timeZone";
        static final String timestampUTC = "timestampUTC";
        static final String timestampLocal = "timestampLocal";
        static final String CRC = "CRC";
    }


}
