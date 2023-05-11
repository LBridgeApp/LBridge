package com.diabetes.lbridge.libre;

import android.content.Context;

import com.oop1.AlgorithmRunner;
import com.oop1.CurrentBg;
import com.oop1.GlucoseUnit;
import com.oop1.HistoricBg;
import com.oop1.LibreSavedState;
import com.oop1.OOPResults;

public class LibreMessage {
    private final long scanTimestampUTC;

    public long getScanTimestampUTC(){
        return this.scanTimestampUTC;
    }

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

    public enum MessageLockingStatus {UNLOCKED, SENSOR_NOT_ACTIVATED, SENSOR_STARTING, SENSOR_EXPIRED, SENSOR_FAILURE, SENSOR_UNKNOWN_STATUS, MESSAGE_ALREADY_ADDED_TO_DB}

    private MessageLockingStatus lockingStatus = MessageLockingStatus.UNLOCKED;

    public MessageLockingStatus getLockingStatus() {
        return lockingStatus;
    }

    private void lockFromAddingToDB(MessageLockingStatus reason) {
        if (lockingStatus == MessageLockingStatus.UNLOCKED) {
            this.lockingStatus = reason;
        }
    }

    public static LibreMessage getInstance(Context context, RawLibreData rawLibreData) throws Exception {
        byte[] patchUID = rawLibreData.getPatchUID();
        String libreSN = PatchUID.decodeSerialNumber(patchUID);
        OOPResults oopResults = AlgorithmRunner.RunAlgorithm(context, rawLibreData, libreSN, false);
        LibreSavedState libreSavedState = oopResults.getLibreSavedState();
        long scanTimestampUTC = oopResults.getCurrentBg().getTimestampUTC();
        return new LibreMessage(rawLibreData, libreSavedState, libreSN, oopResults, scanTimestampUTC);

    }

    public void onAddedToDatabase() {
        this.lockFromAddingToDB(LibreMessage.MessageLockingStatus.MESSAGE_ALREADY_ADDED_TO_DB);
    }

    private LibreMessage(RawLibreData rawLibreData,
                         LibreSavedState libreSavedState,
                         String libreSN,
                         OOPResults oopResults,
                         long scanTimestampUTC) throws Exception {
        this.scanTimestampUTC = scanTimestampUTC;
        this.rawLibreData = rawLibreData;
        this.libreSavedState = libreSavedState;
        this.libreSN = libreSN;
        this.currentBg = oopResults.getCurrentBg();
        this.historicBgs = oopResults.getHistoricBgs();
        this.validateMessage();
        this.blockAnomalousMessage();
    }

    private void blockAnomalousMessage() {
        // блокирует сообщение, чтобы предотвратить отправку в libreview.
        final byte sensorStatus = Payload.getSensorStatus(this.getRawLibreData().getPayload());
        final double sensorTimeMinutes = Payload.getSensorTimeInMinutes(this.getRawLibreData().getPayload());

        if (sensorTimeMinutes >= 60 * 24 * 14.0) {
            lockFromAddingToDB(MessageLockingStatus.SENSOR_EXPIRED);
            return;
        }

        if (sensorTimeMinutes <= 60) {
            lockFromAddingToDB(MessageLockingStatus.SENSOR_STARTING);
            return;
        }

        switch (sensorStatus) {
            case 0x01:
                lockFromAddingToDB(MessageLockingStatus.SENSOR_NOT_ACTIVATED);
                return;
            case 0x02:
                lockFromAddingToDB(MessageLockingStatus.SENSOR_STARTING);
                return;
            case 0x03:
                return;
            case 0x04:
            case 0x05:
                lockFromAddingToDB(MessageLockingStatus.SENSOR_EXPIRED);
                return;
            case 0x06:
                lockFromAddingToDB(MessageLockingStatus.SENSOR_FAILURE);
                return;
            default:
                lockFromAddingToDB(MessageLockingStatus.SENSOR_UNKNOWN_STATUS);
        }
    }


    private void validateMessage() throws Exception {
        if (!RawLibreData.verify(rawLibreData)
                || libreSavedState == null
                || libreSN == null
                || libreSN.length() != 11
                || currentBg == null
                || historicBgs == null
                || currentBg.convertBG(GlucoseUnit.MMOL).getBG() == 0.0) {
            throw new Exception("LibreMessage is not valid.");
        }
    }

    public long getSensorStartTimestampLocal(){
        return Payload.getSensorStartTimestampLocal(this.getRawLibreData().getTimestamp(), this.getRawLibreData().getPayload());
    }
    public long getSensorStartTimestampUTC(){
        return Payload.getSensorStartTimestampUTC(this.getRawLibreData().getTimestamp(), this.getRawLibreData().getPayload());
    }
}