package com.abbottdiabetescare.flashglucose.sensorabstractionservice;

/* loaded from: classes.dex */
public final class AlarmConfiguration {
    private final int highGlucoseAlarmThreshold;
    private final int lowGlucoseAlarmThreshold;

    public AlarmConfiguration(int i, int i2) {
        this.lowGlucoseAlarmThreshold = i;
        this.highGlucoseAlarmThreshold = i2;
    }

    public int getLowGlucoseAlarmThreshold() {
        return this.lowGlucoseAlarmThreshold;
    }

    public int getHighGlucoseAlarmThreshold() {
        return this.highGlucoseAlarmThreshold;
    }
}
