package com.diabetes.lbridge;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class Utils {
    public static LocalDateTime fromUtcToLocal(LocalDateTime utc){
        return utc.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDateTime fromLocalToUTC(LocalDateTime local){
        return local.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public static LocalDateTime unixUTCToTimeUTC(long timestampUTC){
        return Instant.ofEpochMilli(timestampUTC)
                .atZone(ZoneOffset.UTC).toLocalDateTime();
    }

    public static long withoutNanos(long timestamp){
        // некоторые поля оканчиваются на 000, поэтому наносекунды убираются.
        Instant instant = Instant.ofEpochMilli(timestamp);
        Instant instantWithoutNanos = instant.truncatedTo(ChronoUnit.SECONDS);
        return instantWithoutNanos.toEpochMilli();
    }

    public static long unixAsLocal(long timestampUTC){
        // Метод toEpochMilli не учитывает временную зону.
        // Класс LocalDateTime не знает, какая у него временная зона.
        // хитрость в том, что нужно локальное время записать как UTC.
        // тогда именно локальное время будет записано в миллисекунды Unix, а не UTC!.
        Instant utc = Instant.ofEpochMilli(timestampUTC);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(utc, ZoneId.systemDefault());
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public static String byteArrayToHex(byte[] a) {
        if (a == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("0x%02x ", b));
        }
        return sb.toString();
    }
}
