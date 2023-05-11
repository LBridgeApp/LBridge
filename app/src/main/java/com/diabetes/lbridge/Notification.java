package com.diabetes.lbridge;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

public enum Notification {
    CRITICAL_ERROR{
        protected void createChannel() {
            String channelId = this.getChannelId();
            CharSequence name = this.getChannelId();
            int importance = this.getImportance();
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            notificationManager.createNotificationChannel(channel);
        }

        @Override
        protected void showOrUpdate(String contextText) {
            NotificationCompat.Builder builder = this.getBuilder();
            builder.setContentText(contextText);
            notificationManager.notify(this.getId(), builder.build());
        }

        @Override
        protected void cancel() {
            notificationManager.cancel(this.getId());
        }

        @Override
        protected NotificationCompat.Builder getBuilder() {
            return new NotificationCompat.Builder(context, this.getChannelId())
                    .setChannelId(this.getChannelId())
                    .setContentTitle(this.getContentTitle())
                    .setContentText("CRITICAL ERROR OCCURRED")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(this.getPriority())
                    .setColor(Color.RED) // цвет фона
                    .setColorized(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(null));
        }

        protected int getImportance() {
            return NotificationManager.IMPORTANCE_HIGH;
        }


        protected int getPriority() {
            return NotificationCompat.PRIORITY_MAX;
        }


        protected String getContentTitle() {
            return App.getInstance().getApplicationContext().getString(R.string.app_name);
        }
        @Override
        protected int getId() {
            return 2;
        }
        protected String getChannelId() {
            return "CRITICAL_ERROR_CHANNEL";
        }
    },
    SERVICE_STOPPED {
        @Override
        protected void createChannel() {
            String channelId = this.getChannelId();
            CharSequence name = this.getChannelId();
            int importance = this.getImportance();
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            notificationManager.createNotificationChannel(channel);
        }

        @Override
        protected void showOrUpdate(String contextText) {
            NotificationCompat.Builder builder = this.getBuilder();
            builder.setContentText(contextText);
            notificationManager.notify(this.getId(), builder.build());
        }

        @Override
        protected void cancel() {
            notificationManager.cancel(this.getId());
        }

        @Override
        protected int getId() {
            return 3;
        }

        @Override
        protected NotificationCompat.Builder getBuilder() {
            return new NotificationCompat.Builder(context, this.getChannelId())
                    .setChannelId(this.getChannelId())
                    .setContentTitle(this.getContentTitle())
                    .setContentText("IMPORTANT:\n" +
                            "SERVICE HAS STOPPED!\n" +
                            "See logs.")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(this.getPriority())
                    .setColor(Color.RED) // цвет фона
                    .setColorized(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(null));
        }

        protected String getChannelId() {
            return "CRITICAL_ERROR_CHANNEL";
        }

        protected String getContentTitle() {
            return App.getInstance().getApplicationContext().getString(R.string.app_name);
        }

        protected int getPriority() {
            return NotificationCompat.PRIORITY_MAX;
        }

        protected int getImportance() {
            return NotificationManager.IMPORTANCE_HIGH;
        }
    },
    HTTP_SERVER {
        protected void createChannel() {
            String channelId = this.getChannelId();
            CharSequence name = this.getChannelId();
            int importance = this.getImportance();
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            notificationManager.createNotificationChannel(channel);
        }

        @Override
        protected void showOrUpdate(String contextText) {
            NotificationCompat.Builder builder = this.getBuilder();
            builder.setContentText(contextText);
            notificationManager.notify(this.getId(), builder.build());
        }

        @Override
        protected void cancel() {
            notificationManager.cancel(this.getId());
        }

        @Override
        protected NotificationCompat.Builder getBuilder() {
            return new NotificationCompat.Builder(context, this.getChannelId())
                    .setChannelId(this.getChannelId())
                    .setContentTitle(this.getContentTitle())
                    .setContentText("Server is starting...")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(this.getPriority())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(null));
        }

        protected int getImportance() {
            return NotificationManager.IMPORTANCE_NONE;
        }

        protected int getPriority() {
            return NotificationCompat.PRIORITY_MIN;
        }

        protected String getContentTitle() {
            return App.getInstance().getApplicationContext().getString(R.string.app_name);
        }

        protected String getChannelId() {
            return "HTTP_SERVER_CHANNEL";
        }

        @Override
        protected int getId() {
            return 1;
        }
    };

    protected final Context context;
    protected final NotificationManager notificationManager;

    Notification() {
        this.context = App.getInstance().getApplicationContext();
        this.notificationManager = context.getSystemService(NotificationManager.class);
        this.createChannel();
    }

    protected abstract void createChannel();
    protected abstract void showOrUpdate(String contextText);
    protected abstract void cancel();
    protected abstract int getId();
    protected abstract NotificationCompat.Builder getBuilder();
}
