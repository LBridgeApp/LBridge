package com.abbottdiabetescare.flashglucose.sensorabstractionservice;

/* loaded from: classes.dex */
public enum Alarm {
    NOT_DETERMINED(0),
    LOW_GLUCOSE(1),
    PROJECTED_LOW_GLUCOSE(2),
    GLUCOSE_OK(3),
    PROJECTED_HIGH_GLUCOSE(4),
    HIGH_GLUCOSE(5);
    
    private final int value;

    Alarm(int i) {
        this.value = i;
    }

    private static Alarm fromValue(int i) {
        Alarm[] values;
        for (Alarm alarm : values()) {
            if (alarm.value == i) {
                return alarm;
            }
        }
        throw new IllegalArgumentException();
    }

    private int toValue() {
        return this.value;
    }
}
