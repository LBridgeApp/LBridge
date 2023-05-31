package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

import java.util.List;

/* loaded from: classes.dex */
public final class DataProcessingException extends Exception {
    private static final long serialVersionUID = 1;
    private final List<PatchEvent> patchEvents;
    private final byte personalizationIndex;
    private final DataProcessingResult result;

    public DataProcessingException(DataProcessingResult dataProcessingResult, List<PatchEvent> list, byte b) {
        super(dataProcessingResult.toString());
        this.result = dataProcessingResult;
        this.patchEvents = list;
        this.personalizationIndex = b;
    }

    public DataProcessingResult getResult() {
        return this.result;
    }

    public List<PatchEvent> getPatchEvents() {
        return this.patchEvents;
    }

    public byte getPersonalizationIndex() {
        return this.personalizationIndex;
    }
}
