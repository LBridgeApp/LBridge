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
    CurrentBg currentBg;
    HistoricBg[] historicBgArray;

    public OOPResults(CurrentBg currentBg, HistoricBg[] historicBgs){
        this.currentBg = currentBg;
        this.historicBgArray = historicBgs;
    }

    public HistoricBg[] getHistoricBgArray(){
        return this.historicBgArray;
    }

    public CurrentBg getCurrentBgObject(){
        return this.currentBg;
    }
}
