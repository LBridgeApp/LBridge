package com.oop1;

/* loaded from: classes.dex */
public class LibreSavedState {
    private final byte[] attenuationState;
    private final byte[] compositeState;

    LibreSavedState(byte[] attenuationState, byte[] compositeState){
        this.attenuationState = attenuationState;
        this.compositeState = compositeState;
    }

    public byte[] getAttenuationState() {
        return attenuationState;
    }

    public byte[] getCompositeState() {
        return compositeState;
    }

    public String toS() {
        String s;
        String s2;
        String s3 = "" + "{compositeState = ";
        if (this.compositeState == null) {
            s = s3 + "null ";
        } else {
            s = s3 + Utils.byteArrayToHex(this.compositeState);
        }
        String s4 = s + " } attenuationState = {";
        if (this.attenuationState == null) {
            s2 = s4 + "null ";
        } else {
            s2 = s4 + Utils.byteArrayToHex(this.attenuationState);
        }
        return s2 + " }";
    }
}
