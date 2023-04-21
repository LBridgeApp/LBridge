package com.example.nfc_libre_scan.libre;

import android.content.Context;

import com.oop1.AlgorithmRunner;
import com.oop1.CurrentBg;
import com.oop1.HistoricBg;
import com.oop1.LibreSavedState;
import com.oop1.OOPResults;

import java.util.stream.Stream;

public class LibreMessage {

    private final RawLibreData rawLibreData;

    public RawLibreData getRawLibreData() {
        return rawLibreData;
    }

    private final LibreSavedState libreSavedState;

    public LibreSavedState getLibreSavedState() {
        return libreSavedState;
    }

    private final String libreSN;

    public String getLibreSN() {
        return libreSN;
    }

    private final CurrentBg currentBg;

    public CurrentBg getCurrentBg() {
        return currentBg;
    }

    private final HistoricBg[] historicBgs;

    public HistoricBg[] getHistoricBgs() {
        return historicBgs;
    }


    public enum MessageLockingStatus {UNLOCKED, SENSOR_NOT_ACTIVATED, SENSOR_STARTING, SENSOR_EXPIRED, SENSOR_FAILURE, SENSOR_UNKNOWN_STATUS, MESSAGE_ALREADY_SENT}

    private MessageLockingStatus lockingStatus = MessageLockingStatus.UNLOCKED;

    public MessageLockingStatus getLockingStatus() {
        return lockingStatus;
    }

    public void lockForSending(MessageLockingStatus reason) {
        if (lockingStatus == MessageLockingStatus.UNLOCKED) {
            this.lockingStatus = reason;
        }
    }

    public static LibreMessage getInstance(Context context, RawLibreData rawLibreData) throws Exception {
        byte[] patchUID = rawLibreData.getPatchUID();
        String libreSN = PatchUID.decodeSerialNumber(patchUID);
        OOPResults oopResults = AlgorithmRunner.RunAlgorithm(context, rawLibreData, libreSN, false);
        LibreSavedState libreSavedState = oopResults.getLibreSavedState();
        return new LibreMessage(rawLibreData, libreSavedState, libreSN, oopResults);

    }

    private LibreMessage(RawLibreData rawLibreData, LibreSavedState libreSavedState, String libreSN, OOPResults oopResults) throws Exception {
        this.rawLibreData = rawLibreData;
        this.libreSavedState = libreSavedState;
        this.libreSN = libreSN;
        this.currentBg = oopResults.getCurrentBg();
        this.historicBgs = oopResults.getHistoricBgs();
        if (!RawLibreData.verify(rawLibreData)
                || libreSavedState == null
                || libreSN == null
                || currentBg == null
                || historicBgs == null) {
            throw new Exception("LibreMessage is not valid.");
        }

        byte sensorStatus = rawLibreData.getPayload()[4];

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
