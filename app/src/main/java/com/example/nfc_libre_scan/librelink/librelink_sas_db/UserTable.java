package com.example.nfc_libre_scan.librelink.librelink_sas_db;

import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.SensorSelectionRangeRow;
import com.example.nfc_libre_scan.librelink.librelink_sas_db.rows.UserRow;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class UserTable implements Table {
    private final LibreLinkDatabase db;

    public UserTable(LibreLinkDatabase db) {
        this.db = db;
    }

    public int getLastStoredUserId(){
        UserRow[] rows = this.queryRows();
        return rows[rows.length - 1].getUserId();
    }


    @Override
    public String getName() {
        return "users";
    }

    @Override
    public UserRow[] queryRows() {
        List<UserRow> rowList = new ArrayList<>();

        int rowLength = SqlUtils.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new UserRow(this, rowIndex));
        }

        return rowList.toArray(new UserRow[0]);
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return db;
    }
}
