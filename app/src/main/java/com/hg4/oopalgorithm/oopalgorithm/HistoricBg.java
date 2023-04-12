package com.hg4.oopalgorithm.oopalgorithm;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/* compiled from: OOPResultsContainer.java */
/* loaded from: classes.dex */
public class HistoricBg {
    final long scanUnixTimestamp;
    final int historicSensorTime;
    final int currentSensorTime;
    final double bg;
    final int quality;
    final GlucoseUnit glucoseUnit;

    public double getBG(){
        return this.bg;
    }

    public int getHistoricSensorTime() {
        return this.historicSensorTime;
    }

    public GlucoseUnit getGlucoseUnit() {
        return glucoseUnit;
    }

    public HistoricBg convertBG(GlucoseUnit unitToConvert) {
        return new HistoricBg(scanUnixTimestamp, historicSensorTime, currentSensorTime, unitToConvert.convertFrom(bg, this.glucoseUnit), quality, unitToConvert);
    }

    public ZonedDateTime getSensorTimeAsUTC(){
        //oOPResults.timestamp + (historicBg.time - oOPResults.currentTime) * 60000;
        long sensorTimeUnix = this.scanUnixTimestamp + (this.historicSensorTime - this.currentSensorTime) * 60 * 1_000L;

        return Instant.ofEpochMilli(sensorTimeUnix)
                .atZone(ZoneId.of("UTC"));
    }

    public ZonedDateTime getSensorTimeAsLocalTime(){
        ZonedDateTime UTC = getSensorTimeAsUTC();
        ZoneId zoneId = ZoneId.systemDefault();
        return UTC.withZoneSameInstant(zoneId);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public HistoricBg(long scanUnixTimestamp, int historicSensorTime, int currentSensorTime, double bg, int quality, GlucoseUnit glucoseUnit) {
        this.scanUnixTimestamp = scanUnixTimestamp;
        this.bg = bg;
        this.quality = quality;
        this.historicSensorTime = historicSensorTime;
        this.currentSensorTime = currentSensorTime;
        this.glucoseUnit = glucoseUnit;
    }
}
