package com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing;

import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AlarmConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.ApplicationRegion;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.AttenuationConfiguration;
import com.abbottdiabetescare.flashglucose.sensorabstractionservice.NonActionableConfiguration;
import java.util.List;

/* loaded from: classes.dex */
public final class DataProcessingNative implements DataProcessing {
    private final int parserType;

    private native byte getActivationCommand(int i, byte[] bArr);

    private native byte[] getActivationPayload(int i, byte[] bArr, byte[] bArr2, byte b);

    private native boolean getNeedsReaderInfoForActivation(int i);

    private native MemoryRegion getNextRegionToRead(int i, byte[] bArr, byte[] bArr2, byte[] bArr3, int i2);

    private native boolean getPatchTimeValues(int i, byte[] bArr, Out<Integer> out, Out<Integer> out2);

    private native int getProductFamily(int i);

    private native int getTotalMemorySize(int i);

    private native boolean isPatchSupported(int i, byte[] bArr, ApplicationRegion applicationRegion);

    private native DataProcessingResult processScan(int i, AlarmConfiguration alarmConfiguration, NonActionableConfiguration nonActionableConfiguration, AttenuationConfiguration attenuationConfiguration, byte[] bArr, byte[] bArr2, byte[] bArr3, int i2, int i3, int i4, int i5, int i6, byte[] bArr4, byte[] bArr5, Out<Integer> out, Out<Integer> out2, Out<Boolean> out3, Out<Boolean> out4, Out<byte[]> out5, Out<byte[]> out6, Out<byte[]> out7, Out<AlgorithmResults> out8, Out<List<PatchEvent>> out9, Out<Byte> out10);

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public native long getStatusCode(String str, int i, int i2, int i3, boolean z);

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public native void initialize(Object obj);

    static {
        System.loadLibrary("DataProcessing");
    }

    public DataProcessingNative(int i) {
        this.parserType = i;
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public boolean isPatchSupported(byte[] bArr, ApplicationRegion applicationRegion) {
        return isPatchSupported(this.parserType, bArr, applicationRegion);
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public PatchTimeValues getPatchTimeValues(byte[] bArr) {
        Out out = new Out();
        Out out2 = new Out();
        if (getPatchTimeValues(this.parserType, bArr, out, out2)) {
            return new PatchTimeValues((int) out.value(), (int) out2.value());
        }
        return null;
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public int getProductFamily() {
        return getProductFamily(this.parserType);
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public byte getActivationCommand(byte[] bArr) {
        return getActivationCommand(this.parserType, bArr);
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public byte[] getActivationPayload(byte[] bArr, byte[] bArr2, byte b) {
        return getActivationPayload(this.parserType, bArr, bArr2, b);
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public boolean getNeedsReaderInfoForActivation() {
        return getNeedsReaderInfoForActivation(this.parserType);
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public int getTotalMemorySize() {
        return getTotalMemorySize(this.parserType);
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public MemoryRegion getNextRegionToRead(byte[] bArr, byte[] bArr2, byte[] bArr3, int i) {
        return getNextRegionToRead(this.parserType, bArr, bArr2, bArr3, i);
    }

    @Override // com.abbottdiabetescare.flashglucose.sensorabstractionservice.dataprocessing.DataProcessing
    public DataProcessingOutputs processScan(AlarmConfiguration alarmConfiguration, NonActionableConfiguration nonActionableConfiguration, AttenuationConfiguration attenuationConfiguration, byte[] bArr, byte[] bArr2, byte[] bArr3, int i, int i2, int i3, int i4, int i5, byte[] bArr4, byte[] bArr5) throws DataProcessingException {
        Out out = new Out();
        Out out2 = new Out();
        Out out3 = new Out();
        Out out4 = new Out();
        Out out5 = new Out();
        Out out6 = new Out();
        Out out7 = new Out();
        Out out8 = new Out();
        Out out9 = new Out();
        Out out10x = new Out();
        DataProcessingResult processScan = processScan(this.parserType, alarmConfiguration, nonActionableConfiguration, attenuationConfiguration, bArr, bArr2, bArr3, i, i2, i3, i4, i5, bArr4, bArr5, out, out2, out3, out4, out5, out6, out10x, out7, out8, out9);
        if (processScan == DataProcessingResult.SUCCESS) {
            return new DataProcessingOutputs((int) out.value(), (int) out2.value(), (boolean) out3.value(), (boolean) out4.value(), (byte[]) out5.value(), (byte[]) out6.value(), (AlgorithmResults) out7.value());
        }
        throw new DataProcessingException(processScan, (List<PatchEvent>) out8.value(), (byte) out9.value());
    }
}
