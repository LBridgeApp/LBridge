package com.example.nfc_libre_scan;

public class Utils {
    public static byte[] convertByteStringToByteArray(String byteString) {
        if (byteString == null) {return null;}
        String[] hex = byteString.split("\u0020");

        byte[] byteArray = new byte[hex.length];

        for (int i = 0; i < hex.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return byteArray;
    }
}
