package com.hg4.oopalgorithm.oopalgorithm;

import com.abbottdiabetescare.flashglucose.sensorabstractionservice.TrendArrow;
import com.example.nfc_libre_scan.LibreMessage;

public class CurrentBg {
    final long timestamp;

    final double bg;
    final int currentTime;
    final TrendArrow currentTrend;
    final LibreMessage.GlucoseUnit glucoseUnit;

    public CurrentBg(long timestamp, double currentBg, int currentTime, TrendArrow currentTrend, LibreMessage.GlucoseUnit glucoseUnit) {
        this.timestamp = timestamp;
        this.bg = currentBg;
        this.currentTime = currentTime;
        this.currentTrend = currentTrend;
        this.glucoseUnit = glucoseUnit;
    }

    public TrendArrow getCurrentTrend() {
        return currentTrend;
    }

    public double getBG() {
        return bg;
    }

    public CurrentBg convertBG(LibreMessage.GlucoseUnit needToConvert) throws Exception {
        if (needToConvert == LibreMessage.GlucoseUnit.MMOL && this.glucoseUnit == LibreMessage.GlucoseUnit.MGDL) {
            return new CurrentBg(timestamp, bg / 18, currentTime, currentTrend, LibreMessage.GlucoseUnit.MMOL);
        } else if (needToConvert == LibreMessage.GlucoseUnit.MGDL && this.glucoseUnit == LibreMessage.GlucoseUnit.MMOL) {
            return new CurrentBg(timestamp, bg * 18, currentTime, currentTrend, LibreMessage.GlucoseUnit.MGDL);
        }
        throw new Exception("Glucose unit is not present.");
    }

    public int getTime() {
        return this.getTime();
    }

    public LibreMessage.GlucoseUnit getGlucoseUnit() {
        return glucoseUnit;
    }
}
