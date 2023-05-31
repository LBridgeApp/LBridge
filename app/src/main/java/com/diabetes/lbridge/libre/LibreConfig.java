package com.diabetes.lbridge.libre;

public class LibreConfig {
    public static final int WARMUP_MINUTES = 60;
    private static final double WORK_DAYS = 14.5;

    // Вместо официальных 14.0 дней пишем 14.5,
    // чтобы была возможность отправлять
    // последние 12 часов измерений в libreview.
    public static final int WEAR_DURATION_IN_MINUTES = (int) (60 * 24 * WORK_DAYS);
}
