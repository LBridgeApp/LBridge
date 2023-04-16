package com.example.nfc_libre_scan.libre;

import android.content.Context;

import com.oop1.AlgorithmRunner;
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
    private OOPResults oopResults;

    private OOPResults getOopResults() {
        if(oopResults == null){
            this.oopResults = queryOOPResults();
        }
        return oopResults;
    }

    public CurrentBg getCurrentBgObject() {
        return getOopResults().getCurrentBgObject();
    }

    public HistoricBg[] getHistoricBgArray() {
        return getOopResults().getHistoricBgArray();
    }

    private final Context context;

    LibreMessage(byte[] patchUID, byte[] patchInfo, byte[] payload, String libreSN, Context context) {
        this.patchUID = patchUID;
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.context = context;
        this.libreSN = libreSN;
    }

    private OOPResults queryOOPResults() {
        long timestamp = System.currentTimeMillis();
        return AlgorithmRunner.RunAlgorithm(timestamp, context, payload, patchUID, patchInfo, false, libreSN);
    }
}
