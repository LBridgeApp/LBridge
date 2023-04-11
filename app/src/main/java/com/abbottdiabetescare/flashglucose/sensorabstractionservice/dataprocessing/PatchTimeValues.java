package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

/* loaded from: classes.dex */
public class PatchTimeValues {
    private final int warmupPeriodInMinutes;
    private final int wearDurationInMinutes;

    public PatchTimeValues(int i, int i2) {
        this.warmupPeriodInMinutes = i;
        this.wearDurationInMinutes = i2;
    }

    public int getWarmupPeriodInMinutes() {
        return this.warmupPeriodInMinutes;
    }

    public int getWearDurationInMinutes() {
        return this.wearDurationInMinutes;
    }
}
