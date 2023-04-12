package com.hg4.oopalgorithm.oopalgorithm;

import com.abbottdiabetescare.flashglucose.sensorabstractionservice.TrendArrow;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CurrentBg {
    final long scanUnixTimestamp;
    final double bg;
    final int currentSensorTime;
    final TrendArrow currentTrend;
    final GlucoseUnit glucoseUnit;

    public CurrentBg(long scanUnixTimestamp, double currentBg, int currentSensorTime, TrendArrow currentTrend, GlucoseUnit glucoseUnit) {
        this.scanUnixTimestamp = scanUnixTimestamp;
        this.bg = currentBg;
        this.currentSensorTime = currentSensorTime;
        this.currentTrend = currentTrend;
        this.glucoseUnit = glucoseUnit;
    }

    public TrendArrow getCurrentTrend() {
        return currentTrend;
    }

    public double getBG() {
        return bg;
    }

    public CurrentBg convertBG(GlucoseUnit unitToConvert) {
        return new CurrentBg(scanUnixTimestamp, unitToConvert.convertFrom(bg, this.glucoseUnit), currentSensorTime, currentTrend, unitToConvert);
    }

    public long getSensorTimeAsUnix(){
        return this.scanUnixTimestamp;
    }

    public int getCurrentSensorTime() {
        return this.currentSensorTime;
    }

    public ZonedDateTime getSensorTimeAsUTC(){
        //oOPResults.timestamp + (historicBg.time - oOPResults.currentTime) * 60000;
        return Instant.ofEpochMilli(this.scanUnixTimestamp)
                .atZone(ZoneId.of("UTC"));
    }

    public ZonedDateTime getSensorTimeAsLocalTime(){
        ZonedDateTime UTC = getSensorTimeAsUTC();
        ZoneId zoneId = ZoneId.systemDefault();
        return UTC.withZoneSameInstant(zoneId);
    }

    public GlucoseUnit getGlucoseUnit() {
        return glucoseUnit;
    }
}
