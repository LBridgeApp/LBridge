package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class UserTable implements CrcTable {
    private final LibreLinkDatabase db;

    public UserTable(LibreLinkDatabase db) throws Exception {
        this.db = db;
        this.onTableClassInit();
    }

    @Override
    public void onTableClassInit() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.READING);
    }

    @Override
    public void fillByLastRecord() {
        this.name = (String) this.getRelatedValueForLastUserId(TableStrings.name);
        this.userId = ((Long) this.getRelatedValueForLastUserId(TableStrings.userId)).intValue();
        this.CRC = (long) this.getRelatedValueForLastUserId(TableStrings.CRC);
    }

    @Override
    public String getTableName() {
        return TableStrings.TABLE_NAME;
    }

    @Override
    public long computeCRC32() throws IOException {
        CRC32 crc32 = new CRC32();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        dataOutputStream.writeUTF(this.name);

        dataOutputStream.flush();
        crc32.update(byteArrayOutputStream.toByteArray());
        return crc32.getValue();
    }

    @Override
    public long getOriginalCRC() {
        return this.CRC;
    }

    @Override
    public boolean isTableNull() {
        return SqlUtils.isTableNull(this.db.getObject(), TableStrings.TABLE_NAME);
    }

    @Override
    public void onTableChanged() throws Exception {
        SqlUtils.validateCrcAlgorithm(this, SqlUtils.Mode.WRITING);
    }

    protected Integer getLastStoredUserId() {
        return SqlUtils.getLastStoredFieldValue(db.getObject(), TableStrings.userId, TableStrings.TABLE_NAME);
    }

    private Object getRelatedValueForLastUserId(String fieldName) {
        final Integer lastStoredUserId = getLastStoredUserId();
        return SqlUtils.getRelatedValue(db.getObject(), fieldName, TableStrings.TABLE_NAME, TableStrings.userId, lastStoredUserId);
    }

    private String name;
    private int userId;
    private long CRC;

    private static class TableStrings {
        final static String TABLE_NAME = "users";
        final static String name = "name";
        final static String userId = "userId";
        final static String CRC = "CRC";
    }
}
