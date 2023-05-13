package com.diabetes.lbridge.libre;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

public class PatchUID {
    private static final String[] lookupTable =
            {
                    "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                    "A", "C", "D", "E", "F", "G", "H", "J", "K", "L",
                    "M", "N", "P", "Q", "R", "T", "U", "V", "W", "X",
                    "Y", "Z"
            };

    public static String decodeSerialNumber(byte[] patchUID){
        byte[] serialBuffer = new byte[3 + 8];
        System.arraycopy(patchUID, 0, serialBuffer, 3, 8);

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

    public static byte[] encodeSerialNumber(String serialNumber) {
        List<String> lookupTableAsList = Arrays.asList(lookupTable);

        // новая строка без первого символа "0"
        serialNumber = serialNumber.substring(1);

        char[] symbols = serialNumber.toCharArray();

        StringBuilder binary = new StringBuilder();
        for(int i = 0; i < 10; i++){
            char serialNumberCharacter = symbols[i];

            int value = lookupTableAsList.indexOf(String.valueOf(serialNumberCharacter));
            String binaryString = String.format("%5s", Integer.toBinaryString(value & 0xFF))
                    .replace(' ', '0');
            System.out.printf("8: %s%n", binaryString);
            binary.append(binaryString);
        }

        // добавляем нехватающие нули
        for(int i = binary.length(); i < 64; i++){
            binary.append("0");
        }

        byte[] uuidShort = new byte[]{0, 0, 0, 0, 0, 0, 0, 0 };

        for(int i = 0; i < 8; i++){
            String eightBits = binary.substring(8 * i, 8 * (i + 1));
            uuidShort[i] = (byte) Integer.parseInt(eightBits, 2);
        }

        byte[] serialBuffer = new byte[3 + 8];
        for (int i = 2; i < 8; i++) {
            serialBuffer[(2 + 8) - i] = uuidShort[i - 2];
        }

        serialBuffer[9] = 0x07;
        serialBuffer[10] = (byte) 0xe0;

        byte[] patchUID = new byte[8];

        System.arraycopy(serialBuffer, 3, patchUID, 0, 8);

        return patchUID;
    }

    public static byte[] generateFake(){
        // я сравнил серийные номера сенсоров на площадках продаж.
        // вычислил байты, которые не отличаются и которые меняются.

        final byte[] zeroBytes = new byte[]{ 96, -95, 92, 76, -25, 12, -40, 78, 15, -45, 30, -115, -57, -124, 74, 106, 33, 77, -34, 105, 42, -116, -46, -107 };
        final byte[] firstBytes = new byte[]{ -64, 66, 32, -25, -92, -109, -61, -95, 52, 57, 119, 90, 65, -57, 110, 69, -105, -54, 56, -24, 20, 115, 100, -119, 29 };
        final byte[] secondBytes = new byte[]{ 77, 24, 73, -72, 87, -104, 94, 97, 28, 88, -5, 9, 99, 23, 39, 84, -81, 68, -84, 31 };
        final byte[] thirdBytes = new byte[]{ 10, 11 };

        SecureRandom random = new SecureRandom();
        byte[] patchUID = new byte[8];

        patchUID[0] = zeroBytes[random.nextInt(zeroBytes.length)];
        patchUID[1] = firstBytes[random.nextInt(firstBytes.length)];
        patchUID[2] = secondBytes[random.nextInt(secondBytes.length)];
        patchUID[3] = thirdBytes[random.nextInt(thirdBytes.length)];

        // постоянные байты
        patchUID[4] = 0x00;
        patchUID[5] = (byte) 0xa0;
        patchUID[6] = 0x07;
        patchUID[7] = (byte) 0xe0;

        return patchUID;
    }
}
