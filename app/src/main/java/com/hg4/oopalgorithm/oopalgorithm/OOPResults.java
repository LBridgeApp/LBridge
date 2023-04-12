package com.hg4.oopalgorithm.oopalgorithm;

import com.abbottdiabetescare.flashglucose.sensorabstractionservice.TrendArrow;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.GlucoseValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: OOPResultsContainer.java */
/* loaded from: classes.dex */
public class OOPResults {
    private final long scanUnixTimestamp;
    private final int currentSensorTime;
    CurrentBg currentBg;
    HistoricBg[] historicBgArray = new HistoricBg[0];
    public OOPResults(long scanUnixTimestamp, int currentBg, int currentSensorTime, TrendArrow currentTrend) {
        this.scanUnixTimestamp = scanUnixTimestamp;
        this.currentSensorTime = currentSensorTime;
        this.currentBg = new CurrentBg(scanUnixTimestamp, currentBg, currentSensorTime, currentTrend, GlucoseUnit.MGDL);
    }

    public HistoricBg[] getHistoricBgArray(){
        return this.historicBgArray;
    }

    public CurrentBg getCurrentBgObject(){
        return this.currentBg;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setHistoricBgArray(List<GlucoseValue> historicGlucose) {
        if (historicGlucose != null) {
            HistoricBg[] historicBgs = new HistoricBg[historicGlucose.size()];
            for (int i = 0; i < historicGlucose.size(); i++) {
                GlucoseValue glucoseValue = historicGlucose.get(i);
                int quality = glucoseValue.getDataQuality() == 0 ? 0 : 1;
                int historicSensorTime = glucoseValue.getId();
                double bg = glucoseValue.getValue();
                if(quality == 0){
                    HistoricBg historicBg = new HistoricBg(scanUnixTimestamp, historicSensorTime, currentSensorTime, bg, quality, GlucoseUnit.MGDL);
                    historicBgs[i] = historicBg;
                }
            }
            this.historicBgArray = Arrays.stream(historicBgs).filter(Objects::nonNull).toArray(HistoricBg[]::new);
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
