package com.abbottdiabetescare.flashglucose.sensorabstractionservice;

/* loaded from: classes.dex */
public final class NonActionableConfiguration {
    private final boolean isEnabled;
    private final boolean isVelocityCheckEnabled;
    private final int maximumActionableValue;
    private final double maximumPositiveActionableVelocity;
    private final int minimumActionableId;
    private final int minimumActionableValue;
    private final double minimumNegativeActionableVelocity;

    public NonActionableConfiguration(boolean z, boolean z2, int i, int i2, int i3, double d, double d2) {
        this.isEnabled = z;
        this.isVelocityCheckEnabled = z2;
        this.minimumActionableId = i;
        this.minimumActionableValue = i2;
        this.maximumActionableValue = i3;
        this.minimumNegativeActionableVelocity = d;
        this.maximumPositiveActionableVelocity = d2;
    }

    public boolean getIsEnabled() {
        return this.isEnabled;
    }

    public boolean getIsVelocityCheckEnabled() {
        return this.isVelocityCheckEnabled;
    }

    public int getMinimumActionableId() {
        return this.minimumActionableId;
    }

    public int getMinimumActionableValue() {
        return this.minimumActionableValue;
    }

    public int getMaximumActionableValue() {
        return this.maximumActionableValue;
    }

    public double getMinimumNegativeActionableVelocity() {
        return this.minimumNegativeActionableVelocity;
    }

    public double getMaximumPositiveActionableVelocity() {
        return this.maximumPositiveActionableVelocity;
    }
}
