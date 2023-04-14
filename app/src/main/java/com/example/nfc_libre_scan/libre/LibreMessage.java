package com.example.nfc_libre_scan.libre;

import android.content.Context;

import com.oop1.AlgorithmRunner;
import com.oop1.CurrentBg;
import com.oop1.HistoricBg;
import com.oop1.OOPResults;

public class LibreMessage {
    private final PatchUID patchUID;
    private final byte[] patchInfo;
    private final Payload payload;

    public PatchUID getPatchUID() {
        return this.patchUID;
    }
    public byte[] getPatchInfo() {
        return this.patchInfo;
    }

    public Payload getPayload() {
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

    LibreMessage(PatchUID patchUID, byte[] patchInfo, Payload payload, String libreSN, Context context) {
        this.patchUID = patchUID;
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.context = context;
        this.libreSN = libreSN;
    }

    private OOPResults queryOOPResults() {
        long timestamp = System.currentTimeMillis();
        return AlgorithmRunner.RunAlgorithm(timestamp, context, payload.getValue(), patchUID.getValue(), patchInfo, false, libreSN);
    }
}
