package com.hg4.oopalgorithm.oopalgorithm;

import com.abbottdiabetescare.flashglucose.sensorabstractionservice.TrendArrow;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.GlucoseValue;
import com.example.nfc_libre_scan.LibreMessage;

import java.util.List;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: OOPResultsContainer.java */
/* loaded from: classes.dex */
public class OOPResults {
    CurrentBg currentBg;
    HistoricBg[] historicBg;
    public OOPResults(long timestamp, int currentBg, int currentTime, TrendArrow currentTrend) {
        this.currentBg = new CurrentBg(timestamp, currentBg, currentTime, currentTrend, LibreMessage.GlucoseUnit.MGDL);
    }

    public HistoricBg[] getHistoricBgArray(){
        return this.historicBg;
    }

    public CurrentBg getCurrentBgObject(){
        return this.currentBg;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setHistoricBg(List<GlucoseValue> historicGlucose) {
        if (historicGlucose != null) {
            this.historicBg = new HistoricBg[historicGlucose.size()];
            for (int i = 0; i < historicGlucose.size(); i++) {
                GlucoseValue glucoseValue = historicGlucose.get(i);
                int quality = glucoseValue.getDataQuality() == 0 ? 0 : 1;
                int time = glucoseValue.getId();
                double bg = glucoseValue.getValue();
                HistoricBg historicBg = new HistoricBg(time, bg, quality, LibreMessage.GlucoseUnit.MGDL);
                this.historicBg[i] = historicBg;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /*public String toGson() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }*/

    /*public static void HandleData(String oopData) {
        Gson gson = new GsonBuilder().create();
        OOPResultsContainer oOPResultsContainer = (OOPResultsContainer) gson.fromJson(oopData, (Class<Object>) OOPResultsContainer.class);
    }*/
}
