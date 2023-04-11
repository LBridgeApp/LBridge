package com.hg4.oopalgorithm.oopalgorithm;

import com.example.nfc_libre_scan.LibreMessage;

/* compiled from: OOPResultsContainer.java */
/* loaded from: classes.dex */
public class HistoricBg {

    final int time;
    final double bg;
    final int quality;
    final LibreMessage.GlucoseUnit glucoseUnit;

    public double getBG(){
        return this.bg;
    }

    public int getTime() {
        return this.time;
    }

    public LibreMessage.GlucoseUnit getGlucoseUnit() {
        return glucoseUnit;
    }

    public HistoricBg convertBG(LibreMessage.GlucoseUnit needToConvert) throws Exception {
        if (needToConvert == LibreMessage.GlucoseUnit.MMOL && this.glucoseUnit == LibreMessage.GlucoseUnit.MGDL) {
            return new HistoricBg(time, bg / 18, quality, LibreMessage.GlucoseUnit.MMOL);
        } else if (needToConvert == LibreMessage.GlucoseUnit.MGDL && this.glucoseUnit == LibreMessage.GlucoseUnit.MMOL) {
            return new HistoricBg(time, bg * 18, quality, LibreMessage.GlucoseUnit.MGDL);
        }
        throw new Exception("Glucose unit is not present.");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HistoricBg(int time, double bg, int quality, LibreMessage.GlucoseUnit glucoseUnit) {
        this.bg = bg;
        this.quality = quality;
        this.time = time;
        this.glucoseUnit = glucoseUnit;
    }
}
