package com.diabetes.lbridge.librelink;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.diabetes.lbridge.Logger;
import com.diabetes.lbridge.Utils;
import com.diabetes.lbridge.WebService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class PeriodicTimer extends BroadcastReceiver {
    private final LibreLink caller;
    private final AlarmManager alarmManager;
    private final PendingIntent pendingIntent;
    private boolean timerIsActive = false;

    public PeriodicTimer(LibreLink caller, WebService context) {
        this.caller = caller;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent("com.diabetes.lbridge.librelink.timerReceiver");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        IntentFilter filter = new IntentFilter("com.diabetes.lbridge.librelink.timerReceiver");
        context.registerReceiver(this, filter);
        context.addBroadcastReceiver(this);
    }

    protected void start() {
        timerIsActive = true;
        planNextTask(TaskStatus.START_TIMER);
    }
    protected void stop() {
        timerIsActive = false;
        alarmManager.cancel(pendingIntent);
    }

    private TaskStatus runTask(){
        TaskStatus status;
        try {
            caller.addLastScanToDatabase();
            status = TaskStatus.SUCCESS;
        } catch (Exception e) {
            Logger.error(e);
            status = TaskStatus.FAILURE;
        }
        Logger.inf(String.format("Task has run. Task status: %s", status));
        return status;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(timerIsActive){
            TaskStatus taskStatus = runTask();
            this.planNextTask(taskStatus);
        }
    }

    private void planNextTask(TaskStatus taskStatus) {
        int minutes = taskStatus.getNextTaskMinutes();
        long triggerTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes);

        LocalDateTime nextUTC = LocalDateTime.ofEpochSecond(triggerTime / 1_000, 0, ZoneOffset.UTC);
        LocalDateTime nextLocal = Utils.fromUtcToLocal(nextUTC);

        String nextLocalString = String.format("%s",
                nextLocal.format(DateTimeFormatter.ofPattern("HH:mm")));

        Logger.inf(String.format("Next task scheduled at %s", nextLocalString));

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private enum TaskStatus {
        // TODO: поменять время, если установлено тестовое.
        START_TIMER{
            int getNextTaskMinutes(){
                // Данные приходят каждые 5 минут.
                // Отправляем через шесть.
                return 6;
                //return 1;
            }
        },
        SUCCESS {
            int getNextTaskMinutes() {
                /* рандомизуем время отправки.
                * Следующая отправка минимум через час,
                * максимум через два часа.
                */
                Random random = new Random();
                int randomMinutes = random.nextInt(61); // from 0 to 60 minutes
                return 60 + randomMinutes; // from 60 to 120 minutes.
                //return 1;
            }
        }, FAILURE {
            int getNextTaskMinutes() {
                // Данные приходят каждые 5 минут.
                // Отправляем через шесть.
                return 6;
                //return 1;
            }
        };

        abstract int getNextTaskMinutes();
    }
}
