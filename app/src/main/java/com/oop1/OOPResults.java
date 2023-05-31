package com.oop1;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: OOPResultsContainer.java */
/* loaded from: classes.dex */
public class OOPResults {
    private final CurrentBg currentBg;
    private final HistoricBg[] historicBgArray;

    private final LibreSavedState libreSavedState;

    public OOPResults(CurrentBg currentBg, HistoricBg[] historicBgs, LibreSavedState libreSavedState){
        this.currentBg = currentBg;
        this.historicBgArray = historicBgs;
        this.libreSavedState = libreSavedState;
    }

    public HistoricBg[] getHistoricBgs(){
        return this.historicBgArray;
    }

    public CurrentBg getCurrentBg(){
        return this.currentBg;
    }

    public LibreSavedState getLibreSavedState(){ return this.libreSavedState; }
}
