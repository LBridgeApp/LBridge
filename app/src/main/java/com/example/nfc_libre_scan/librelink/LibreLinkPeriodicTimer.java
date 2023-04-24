package com.example.nfc_libre_scan.librelink;

import android.content.Context;

import com.example.nfc_libre_scan.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LibreLinkPeriodicTimer {
    private final LibreLink caller;
    private Timer timer;
    LibreLinkPeriodicTimer(LibreLink caller){
        this.caller = caller;
    }

    protected void start(){
        this.timer = new Timer();
        this.scheduleNewSending(0);
    }

    protected void stop(){
        if(timer != null){
            timer.cancel();
        }
        this.timer = null;
    }

    private void scheduleNewSending(long delayMillis){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    caller.addLastScanToDatabase();
                    onSuccess();
                } catch (Exception e) {
                    onFailure();
                    Logger.error(e);
                }
            }
        }, delayMillis);
    }

    private void onSuccess(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.plusMinutes(5);
        long delayMillis = Duration.between(now, next).toMillis();
        Logger.ok(String.format("Sending task done.\n" +
                "Next task scheduled at %s", next.format(DateTimeFormatter.ofPattern("HH:mm"))));
        this.scheduleNewSending(delayMillis);
    }

    private void onFailure(){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.plusMinutes(1);
        long delayMillis = Duration.between(now, next).toMillis();
        Logger.error(String.format("Sending task failed.\n" +
                "Next task scheduled at %s", next.format(DateTimeFormatter.ofPattern("HH:mm"))));
        this.scheduleNewSending(delayMillis);
    }
}
