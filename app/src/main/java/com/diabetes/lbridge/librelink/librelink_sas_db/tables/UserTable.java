package com.diabetes.lbridge.librelink.librelink_sas_db.tables;

import com.diabetes.lbridge.librelink.librelink_sas_db.LibreLinkDatabase;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.CrcRow;
import com.diabetes.lbridge.librelink.librelink_sas_db.rows.UserRow;

import java.util.ArrayList;
import java.util.List;

public class UserTable implements Table, CrcTable {
    private final LibreLinkDatabase db;
    private UserRow[] rows;

    public UserTable(LibreLinkDatabase db) {
        this.db = db;
        this.rows = this.queryRows();
    }

    public int getLastStoredUserId(){
        return (rows.length != 0) ? rows[rows.length - 1].getUserId() : 0;
    }

    @Override
    public String getName() {
        return "users";
    }

    @Override
    public UserRow[] queryRows() {
        List<UserRow> rowList = new ArrayList<>();

        int rowLength = Table.getRowLength(db.getSQLite(), this);
        for(int rowIndex = 0; rowIndex < rowLength; rowIndex++){
            rowList.add(new UserRow(this, rowIndex));
        }

        return rowList.toArray(new UserRow[0]);
    }

    @Override
    public void rowInserted() {
        this.rows = this.queryRows();
    }

    @Override
    public LibreLinkDatabase getDatabase() {
        return db;
    }

    @Override
    public void validateCRC() throws Exception {
        for(CrcRow row : rows){
            row.validateCRC();
        }
    }
}
