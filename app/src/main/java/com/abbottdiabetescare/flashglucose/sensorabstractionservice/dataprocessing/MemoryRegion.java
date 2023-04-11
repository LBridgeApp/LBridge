package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

/* loaded from: classes.dex */
public class MemoryRegion {
    private final int numberOfBytes;
    private final int startAddress;

    public MemoryRegion(int i, int i2) {
        this.startAddress = i;
        this.numberOfBytes = i2;
    }

    public int getStartAddress() {
        return this.startAddress;
    }

    public int getNumberOfBytes() {
        return this.numberOfBytes;
    }
}
