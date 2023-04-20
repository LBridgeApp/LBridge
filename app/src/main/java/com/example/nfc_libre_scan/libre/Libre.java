package com.example.nfc_libre_scan.libre;

import android.content.Context;

import com.oop1.AlgorithmRunner;
import com.oop1.OOPResults;

public class Libre {
    private Context context;
    private final RawLibreData rawLibreData;
    private byte[] patchUID;
    public Libre(Context context, RawLibreData rawLibreData){
        this.context = context;
        this.rawLibreData = rawLibreData;
    }

    public LibreMessage getLibreMessage() throws Exception {
        this.patchUID = rawLibreData.getPatchUID();
        byte[] patchInfo = rawLibreData.getPatchInfo();
        byte[] payload = rawLibreData.getPayload();
        long timestamp = rawLibreData.getTimestamp();
        String libreSN = PatchUID.decodeSerialNumber();
        OOPResults oopResults = AlgorithmRunner.RunAlgorithm(timestamp, context, payload, patchUID, patchInfo, false, libreSN);
        return new LibreMessage(rawLibreData, libreSN, oopResults );
    }
}
