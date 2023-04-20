package com.example.nfc_libre_scan.libre;

import com.oop1.CurrentBg;
import com.oop1.HistoricBg;
import com.oop1.OOPResults;

public class LibreMessage {
    private final byte[] patchUID;
    private final byte[] patchInfo;
    private final byte[] payload;

    public byte[] getPatchUID() {
        return this.patchUID;
    }

    public byte[] getPatchInfo() {
        return this.patchInfo;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    private final String libreSN;

    public String getLibreSN() {
        return libreSN;
    }

    private final OOPResults oopResults;

    public enum MessageLockingStatus {UNLOCKED, SENSOR_NOT_ACTIVATED, SENSOR_STARTING, SENSOR_EXPIRED, SENSOR_FAILURE, SENSOR_UNKNOWN_STATUS, MESSAGE_ALREADY_SENT}

    private MessageLockingStatus lockingStatus = MessageLockingStatus.UNLOCKED;

    public MessageLockingStatus getLockingStatus(){ return lockingStatus; }

    public void lockForSending(MessageLockingStatus reason) {
        if(lockingStatus == MessageLockingStatus.UNLOCKED){
            this.lockingStatus = reason;
        }
    }

    public CurrentBg getCurrentBgObject() {
        return oopResults.getCurrentBgObject();
    }

    public HistoricBg[] getHistoricBgArray() {
        return oopResults.getHistoricBgArray();
    }

    public LibreMessage(RawLibreData rawLibreData, String libreSN, OOPResults oopResults) throws Exception {
        this.patchUID = rawLibreData.getPatchUID();
        this.patchInfo = rawLibreData.getPatchInfo();
        this.payload = rawLibreData.getPayload();
        this.libreSN = libreSN;
        this.oopResults = oopResults;

        if (patchUID == null
                || patchInfo == null
                || payload == null
                || libreSN == null
                || oopResults == null) {
            throw new Exception("LibreMessage is not valid.");
        }

        byte sensorStatus = payload[4];

        switch (sensorStatus) {
            case 0x01:
                lockForSending(MessageLockingStatus.SENSOR_NOT_ACTIVATED);
                break;
            case 0x02:
                lockForSending(MessageLockingStatus.SENSOR_STARTING);
                break;
            case 0x03:
                break;
            case 0x04:
            case 0x05:
                lockForSending(MessageLockingStatus.SENSOR_EXPIRED);
                break;
            case 0x06:
                lockForSending(MessageLockingStatus.SENSOR_FAILURE);
                break;
            default:
                lockForSending(MessageLockingStatus.SENSOR_UNKNOWN_STATUS);
        }
    }
}
