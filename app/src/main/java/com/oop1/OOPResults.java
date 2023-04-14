package com.oop1;

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
