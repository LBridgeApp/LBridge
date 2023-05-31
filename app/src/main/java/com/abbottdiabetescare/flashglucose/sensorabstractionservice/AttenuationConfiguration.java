package com.abbottdiabetescare.flashglucose.sensorabstractionservice;

/* loaded from: classes.dex */
public class AttenuationConfiguration {
    private final boolean isEsaCorrectionEnabled;
    private final boolean isEsaDetectionEnabled;
    private final boolean isLsaCorrectionEnabled;
    private final boolean isLsaDetectionEnabled;
    private final int minimumSensorLifeToEnable;

    public AttenuationConfiguration(int i, boolean z, boolean z2, boolean z3, boolean z4) {
        this.minimumSensorLifeToEnable = i;
        this.isEsaDetectionEnabled = z;
        this.isLsaDetectionEnabled = z2;
        this.isEsaCorrectionEnabled = z3;
        this.isLsaCorrectionEnabled = z4;
    }

    public int getMinimumSensorLifeToEnable() {
        return this.minimumSensorLifeToEnable;
    }

    public boolean isEsaDetectionEnabled() {
        return this.isEsaDetectionEnabled;
    }

    public boolean isLsaDetectionEnabled() {
        return this.isLsaDetectionEnabled;
    }

    public boolean isEsaCorrectionEnabled() {
        return this.isEsaCorrectionEnabled;
    }

    public boolean isLsaCorrectionEnabled() {
        return this.isLsaCorrectionEnabled;
    }
}
