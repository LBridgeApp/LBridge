package com.abbottdiabetescare.flashglucose.sensorabstractionservice;

/* loaded from: classes.dex */
public enum TrendArrow {
    NOT_DETERMINED(0),
    FALLING_QUICKLY(1),
    FALLING(2),
    STABLE(3),
    RISING(4),
    RISING_QUICKLY(5);
    
    private final int value;

    TrendArrow(int i) {
        this.value = i;
    }

    private static TrendArrow fromValue(int i) {
        TrendArrow[] values;
        for (TrendArrow trendArrow : values()) {
            if (trendArrow.value == i) {
                return trendArrow;
            }
        }
        throw new IllegalArgumentException();
    }

    private int toValue() {
        return this.value;
    }
}
