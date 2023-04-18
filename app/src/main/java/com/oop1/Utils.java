package com.oop1;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Formatter;

/* loaded from: classes.dex */
public class Utils {
    public static long getUtcTimestampAsLocal(long timestampUTC){
        // Метод toEpochMilli не учитывает временную зону.
        // Класс LocalDateTime не знает, какая у него временная зона.
        // хитрость в том, что нужно локальное время записать как UTC.
        // тогда именно локальное время будет записано в миллисекунды Unix, а не UTC!.
        Instant utc = Instant.ofEpochMilli(timestampUTC);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(utc, ZoneId.systemDefault());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneOffset.UTC);
        return zonedDateTime.toInstant().toEpochMilli();
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
}
