package com.example.nfc_libre_scan.libre;

public class PatchUID {
    public static String decodeSerialNumber(byte[] patchUID){
        byte[] serialBuffer = new byte[3 + 8];
        System.arraycopy(patchUID, 0, serialBuffer, 3, 8);

        String[] lookupTable =
                {
                        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                        "A", "C", "D", "E", "F", "G", "H", "J", "K", "L",
                        "M", "N", "P", "Q", "R", "T", "U", "V", "W", "X",
                        "Y", "Z"
                };
        byte[] uuidShort = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        int i;

        for (i = 2; i < 8; i++) uuidShort[i - 2] = serialBuffer[(2 + 8) - i];
        uuidShort[6] = 0x00;
        uuidShort[7] = 0x00;

        StringBuilder binary = new StringBuilder();
        String binS;
        for (i = 0; i < 8; i++) {
            binS = String.format("%8s", Integer.toBinaryString(uuidShort[i] & 0xFF)).replace(' ', '0');
            binary.append(binS);
        }

        StringBuilder v = new StringBuilder("0");
        char[] pozS = {0, 0, 0, 0, 0};
        for (i = 0; i < 10; i++) {
            for (int k = 0; k < 5; k++) pozS[k] = binary.charAt((5 * i) + k);
            int value = (pozS[0] - '0') * 16 + (pozS[1] - '0') * 8 + (pozS[2] - '0') * 4 + (pozS[3] - '0') * 2 + (pozS[4] - '0') * 1;
            v.append(lookupTable[value]);
        }

        return v.toString();
    }
}
