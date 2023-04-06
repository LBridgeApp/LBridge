package com.example.nfc_libre_scan;

import com.example.nfc_libre_scan.librelink_db.RawScan;

public class AppTester {

    public boolean runTests(){
        boolean testA = RawScan.testCRC32();
        return testA;
    }
}
