package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

/* loaded from: classes.dex */
public final class PatchEvent {
    private final int errorCode;
    private final int id;

    public PatchEvent(int i, int i2) {
        this.id = i;
        this.errorCode = i2;
    }

    public int getId() {
        return this.id;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
