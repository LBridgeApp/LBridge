package com.oop1;

import com.diabetes.lbridge.Utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/* compiled from: OOPResultsContainer.java */
/* loaded from: classes.dex */
public class HistoricBg {
    final long scanUnixTimestamp;
    final int historicSensorTime;
    final int currentSensorTime;
    final double bg;
    final int quality;
    final GlucoseUnit glucoseUnit;

    public HistoricBg(long scanUnixTimestamp, int historicSensorTime, int currentSensorTime, double bg, int quality, GlucoseUnit glucoseUnit) {
        this.scanUnixTimestamp = scanUnixTimestamp;
        this.bg = bg;
        this.quality = quality;
        this.historicSensorTime = historicSensorTime;
        this.currentSensorTime = currentSensorTime;
        this.glucoseUnit = glucoseUnit;
    }

    public double getBG() {
        return this.bg;
    }

    public int getSampleNumber() {
        // sampleNumber - это так поле называется в базе данных LibreLink
        return this.historicSensorTime;
    }

    public long getTimestampUTC(){
        return this.scanUnixTimestamp + (this.historicSensorTime - this.currentSensorTime) * 60 * 1_000L;
    }

    public long getTimestampLocal() {
        return Utils.unixAsLocal(this.getTimestampUTC());
    }

    public String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    public GlucoseUnit getGlucoseUnit() {
        return glucoseUnit;
    }

    public HistoricBg convertBG(GlucoseUnit unitToConvert) {
        return new HistoricBg(scanUnixTimestamp, historicSensorTime, currentSensorTime, unitToConvert.convertFrom(bg, this.glucoseUnit), quality, unitToConvert);
    }

    public ZonedDateTime getSensorTimeAsUTC() {
        return Instant.ofEpochMilli(this.getTimestampUTC())
                .atZone(ZoneOffset.UTC);
    }

    public ZonedDateTime getSensorTimeAsLocalTime() {
        ZonedDateTime UTC = getSensorTimeAsUTC();
        ZoneId zoneId = ZoneId.systemDefault();
        return UTC.withZoneSameInstant(zoneId);
    }
}
