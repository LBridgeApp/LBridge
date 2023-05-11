package com.diabetes.lbridge;

import android.content.Context;
import android.os.VibrationEffect;
import androidx.core.content.ContextCompat;

public enum Vibrator {
    SCAN_START(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)),
    SCAN_ERROR(VibrationEffect.createWaveform(new long[] {0, 200, 200, 200}, -1)),
    SCAN_SUCCESS(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)),
    CRITICAL_ERROR(VibrationEffect.createWaveform(new long[] {0, 100, 200, 100, 200, 100, 200, 100, 200, 100}, -1));
    private final VibrationEffect vibrationEffect;
    Vibrator(VibrationEffect vibrationEffect){
        this.vibrationEffect = vibrationEffect;
    }

    public void vibrate(Context context){
        android.os.Vibrator vibrator = (android.os.Vibrator) ContextCompat.getSystemService(context, android.os.Vibrator.class);
        if(vibrator != null){
            vibrator.vibrate(vibrationEffect);
        }
    }
}
