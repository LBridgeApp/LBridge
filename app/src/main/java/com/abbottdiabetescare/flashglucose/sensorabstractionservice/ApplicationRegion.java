package com.abbottdiabetescare.flashglucose.sensorabstractionservice;

/* loaded from: classes.dex */
public enum ApplicationRegion {
    LEVEL_1(0),
    LEVEL_2(1),
    LEVEL_3(2),
    LEVEL_4(3),
    LEVEL_5(4),
    LEVEL_6(5);
    
    private final int value;

    ApplicationRegion(int i) {
        this.value = i;
    }

    private static ApplicationRegion fromValue(int i) {
        ApplicationRegion[] values;
        for (ApplicationRegion applicationRegion : values()) {
            if (applicationRegion.value == i) {
                return applicationRegion;
            }
        }
        throw new IllegalArgumentException();
    }

    private int toValue() {
        return this.value;
    }
}
