package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

/* loaded from: classes.dex */
public final class GlucoseValue {
    private final int dataQuality;
    private final int id;
    private final int value;

    public GlucoseValue(int i, int i2, int i3) {
        this.id = i;
        this.dataQuality = i2;
        this.value = i3;
    }

    public int getId() {
        return this.id;
    }

    public int getDataQuality() {
        return this.dataQuality;
    }

    public int getValue() {
        return this.value;
    }
}
