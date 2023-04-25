package com.example.nfc_libre_scan.librelink;

import android.content.Context;

import com.example.nfc_libre_scan.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Random;
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
        LocalDateTime now = LocalDateTime.now();
        // Данные приходят каждые 5 минут.
        // Первый раз отправляем через шесть.
        LocalDateTime next = now.plusMinutes(6);

        //TODO: исправить время, если установлено.
        //LocalDateTime next = now.plusMinutes(1);

        long delayMillis = Duration.between(now, next).toMillis();
        Logger.inf(String.format("Periodic timer started.\n" +
                "Next task scheduled at %s", next.format(DateTimeFormatter.ofPattern("HH:mm"))));
        this.scheduleNewSending(delayMillis);
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
                    Logger.error(e);
                    onFailure();
                }
            }
        }, delayMillis);
    }

    private void onSuccess(){
        LocalDateTime now = LocalDateTime.now();
        // рандомизуем частоту отправки
        final int randomMinutes = new Random().nextInt(61); // from 0 to 60
        // следующая отправка, если последняя была успешна, через 60 - 120 минут.
        LocalDateTime next = now.plusMinutes(60).plusMinutes(randomMinutes);

        //TODO: исправить время, если установлено.
        //LocalDateTime next = now.plusMinutes(1);

        long delayMillis = Duration.between(now, next).toMillis();
        Logger.ok(String.format("Sending task done.\n" +
                "Next task scheduled at %s", next.format(DateTimeFormatter.ofPattern("HH:mm"))));
        this.scheduleNewSending(delayMillis);
    }

    private void onFailure(){
        LocalDateTime now = LocalDateTime.now();
        // Данные приходят каждые 5 минут.
        // Следующая отправка через шесть.
        LocalDateTime next = now.plusMinutes(6);

        //TODO: исправить время, если установлено.
        //LocalDateTime next = now.plusMinutes(1);

        long delayMillis = Duration.between(now, next).toMillis();
        Logger.error(String.format("Sending task failed.\n" +
                "Next task scheduled at %s", next.format(DateTimeFormatter.ofPattern("HH:mm"))));
        this.scheduleNewSending(delayMillis);
    }
}
