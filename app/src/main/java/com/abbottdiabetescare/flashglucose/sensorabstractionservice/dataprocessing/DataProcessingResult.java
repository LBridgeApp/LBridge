package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

/* loaded from: classes.dex */
public enum DataProcessingResult {
    SUCCESS(0),
    RESTART_SENSOR_STORAGE_STATE(1),
    RESCAN_SENSOR_BAD_CRC(2),
    TERMINATE_SENSOR_NORMAL_TERMINATED_STATE(3),
    TERMINATE_SENSOR_ERROR_TERMINATED_STATE(4),
    TERMINATE_SENSOR_CORRUPT_PAYLOAD(5),
    FATAL_ERROR_BAD_ARGUMENTS(6);
    
    private final int value;

    DataProcessingResult(int i) {
        this.value = i;
    }

    private static DataProcessingResult fromValue(int i) {
        DataProcessingResult[] values;
        for (DataProcessingResult dataProcessingResult : values()) {
            if (dataProcessingResult.value == i) {
                return dataProcessingResult;
            }
        }
        throw new IllegalArgumentException();
    }

    private int toValue() {
        return this.value;
    }
}
