package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AlarmConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.ApplicationRegion;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AttenuationConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.NonActionableConfiguration;

/* loaded from: classes.dex */
public interface DataProcessing {
    public static final int BASE_YEAR = 2010;

    byte getActivationCommand(byte[] bArr);

    byte[] getActivationPayload(byte[] bArr, byte[] bArr2, byte b);

    boolean getNeedsReaderInfoForActivation();

    MemoryRegion getNextRegionToRead(byte[] bArr, byte[] bArr2, byte[] bArr3, int i);

    PatchTimeValues getPatchTimeValues(byte[] bArr);

    int getProductFamily();

    long getStatusCode(String str, int i, int i2, int i3, boolean z);

    int getTotalMemorySize();

    void initialize(Object obj);

    boolean isPatchSupported(byte[] bArr, ApplicationRegion applicationRegion);

    DataProcessingOutputs processScan(AlarmConfiguration alarmConfiguration, NonActionableConfiguration nonActionableConfiguration, AttenuationConfiguration attenuationConfiguration, byte[] bArr, byte[] bArr2, byte[] bArr3, int i, int i2, int i3, int i4, int i5, byte[] bArr4, byte[] bArr5) throws DataProcessingException;
}
