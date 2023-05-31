package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

/* loaded from: classes.dex */
public final class DataProcessingOutputs {
    private final AlgorithmResults algorithmResults;
    private final int estimatedSensorEndTimestamp;
    private final int estimatedSensorStartTimestamp;
    private final boolean insertionIsConfirmed;
    private final byte[] newAttenuationState;
    private final byte[] newCompositeState;
    private final boolean sensorHasBeenRemoved;

    public DataProcessingOutputs(int i, int i2, boolean z, boolean z2, byte[] bArr, byte[] bArr2, AlgorithmResults algorithmResults) {
        this.estimatedSensorStartTimestamp = i;
        this.estimatedSensorEndTimestamp = i2;
        this.insertionIsConfirmed = z;
        this.sensorHasBeenRemoved = z2;
        this.newCompositeState = bArr;
        this.newAttenuationState = bArr2;
        this.algorithmResults = algorithmResults;
    }

    public int getEstimatedSensorStartTimestamp() {
        return this.estimatedSensorStartTimestamp;
    }

    public int getEstimatedSensorEndTimestamp() {
        return this.estimatedSensorEndTimestamp;
    }

    public boolean getInsertionIsConfirmed() {
        return this.insertionIsConfirmed;
    }

    public boolean getSensorHasBeenRemoved() {
        return this.sensorHasBeenRemoved;
    }

    public byte[] getNewCompositeState() {
        return this.newCompositeState;
    }

    public byte[] getNewAttenuationState() {
        return this.newAttenuationState;
    }

    public AlgorithmResults getAlgorithmResults() {
        return this.algorithmResults;
    }
}
