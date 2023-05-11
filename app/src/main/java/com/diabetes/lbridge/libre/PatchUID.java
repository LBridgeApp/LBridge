package com.diabetes.lbridge.libre;

import android.util.Log;

import com.diabetes.lbridge.App;
import com.diabetes.lbridge.Utils;

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

        serialNumber = serialNumber.substring(1); // новая строка без первого символа "0"

        Log.v(App.TAG, String.format("1. Serial number: %s%n", serialNumber));

        char[] symbols = serialNumber.toCharArray();

        Log.v(App.TAG, String.format("2. Char array: %s%n", Arrays.toString(symbols)));

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

        Log.v(App.TAG, String.format("3. uuidShort: %s%n", Arrays.toString(uuidShort)));
        Log.v(App.TAG, String.format("3. uuidShort: %s%n", Utils.byteArrayToHex(uuidShort)));

        byte[] serialBuffer = new byte[3 + 8];
        for (int i = 2; i < 8; i++) {
            serialBuffer[(2 + 8) - i] = uuidShort[i - 2];
        }

        serialBuffer[9] = 0x07;
        serialBuffer[10] = (byte) 0xe0;

        Log.v(App.TAG, String.format("4. serialBuffer: %s%n", Arrays.toString(serialBuffer)));
        Log.v(App.TAG, String.format("4. serialBuffer: %s%n", Utils.byteArrayToHex(serialBuffer)));

        byte[] patchUID = new byte[8];

        System.arraycopy(serialBuffer, 3, patchUID, 0, 8);

        Log.v(App.TAG, String.format("5. PatchUID: %s%n", Arrays.toString(patchUID)));
        Log.v(App.TAG, String.format("5. PatchUID: %s%n", Utils.byteArrayToHex(patchUID)));

        return patchUID;
    }

    public static byte[] generateFake(){
        // TODO: подумать, может не стоит писать сюда определенные 2-ые байты. Могут вычислить покупателя сенсоров.
        // я сравнил серийные номера сенсоров
        // вычислил байты, которые не отличаются,
        // которые повторяются несколько раз,
        // которые встречаются только один раз.

        SecureRandom random = new SecureRandom();
        byte[] patchUID = new byte[8];

        patchUID[0] = (byte) (Byte.MIN_VALUE + random.nextInt((Byte.MAX_VALUE + 1) * 2));
        patchUID[1] = (byte) (Byte.MIN_VALUE + random.nextInt((Byte.MAX_VALUE + 1) * 2));
        byte[] secondBytes = new byte[]{ 80, -80, 112, 99 };
        patchUID[2] = secondBytes[random.nextInt(secondBytes.length)];
        // постоянные байты
        patchUID[3] = 0x0b;
        patchUID[4] = 0x00;
        patchUID[5] = (byte) 0xa0;
        patchUID[6] = 0x07;
        patchUID[7] = (byte) 0xe0;

        return patchUID;
    }
}
