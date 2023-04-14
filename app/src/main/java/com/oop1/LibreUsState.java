package com.oop1;

/* loaded from: classes.dex */
public class LibreUsState {
    public byte[] attenuationState;
    public byte[] compositeState;

    public String toS() {
        String s;
        String s2;
        String s3 = new String() + "{compositeState = ";
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
