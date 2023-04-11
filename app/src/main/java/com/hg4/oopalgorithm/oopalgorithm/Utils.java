package com.hg4.oopalgorithm.oopalgorithm;

import android.content.Context;
import android.util.Log;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

/* loaded from: classes.dex */
public class Utils {
    static final String TAG = "xOOPAlgorithm";

    public static String objectToString(Object obj) {
        if (obj == null) {
            return "{null}";
        }
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        result.append(obj.getClass().getName());
        result.append(" Object {");
        result.append(newLine);
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                field.setAccessible(true);
                result.append(field.get(obj));
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");
        return result.toString();
    }

    public static String byteArrayToHex(byte[] a) {
        if (a == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("0x%02x ", Byte.valueOf(b)));
        }
        return sb.toString();
    }

    public static String byteArrayToString(byte[] byteArr) {
        Formatter formatter = new Formatter();
        for (byte b : byteArr) {
            formatter.format("%02x", Byte.valueOf(b));
        }
        return formatter.toString();
    }

    public static boolean comparePartialByteArray(byte[] b1, byte[] b2) {
        int len = Math.min(b1.length, b2.length);
        for (int i = 0; i < len; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] stringToByte(String str) {
        String[] str_array = str.split(",");
        byte[] numbers = new byte[str_array.length];
        for (int i = 0; i < str_array.length; i++) {
            try {
                numbers[i] = Byte.decode(str_array[i].trim()).byteValue();
            } catch (NumberFormatException e) {
                Log.i(TAG, "Invalid value for a byte in " + str);
                Log.i(TAG, "Invalid value index is " + i + " value is " + str_array[i].trim());
                return null;
            }
        }
        return numbers;
    }

    public static byte[] readBinaryFile(String fullPath) {
        if (fullPath == null) {
            return null;
        }
        File file = new File(fullPath);
        byte[] fileData = new byte[(int) file.length()];
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(file));
            try {
                dis.readFully(fileData);
                dis.close();
                return fileData;
            } catch (IOException e) {
                Log.i(TAG, "Error reading from file " + fullPath);
                return null;
            }
        } catch (FileNotFoundException e2) {
            Log.i(TAG, "File not found " + fullPath);
            return null;
        }
    }

    public static void writeToFile(Context context, String file, byte[] data) {
        Log.i(TAG, "context = " + context);
        String dir = context.getFilesDir().getPath();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        String file_name = dir + '/' + file + "_" + currentDateandTime + ".dat";
        try {
            Log.i(TAG, "Writing to file " + file_name + ", size = " + (data == null ? 0 : data.length));
            FileOutputStream f = new FileOutputStream(new File(file_name));
            if (data != null) {
                f.write(data);
            }
            f.close();
        } catch (IOException e) {
            Log.i(TAG, "Cought exception when trying to write file" + e);
        }
    }
}
