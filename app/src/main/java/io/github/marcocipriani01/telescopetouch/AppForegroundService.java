package io.github.marcocipriani01.telescopetouch;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import io.github.marcocipriani01.telescopetouch.activities.MainActivity;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class AppForegroundService extends Service {

    public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String ACTION_EXIT = "ACTION_EXIT";
    public static final String ACTION_DISCONNECT = "ACTION_DISCONNECT";
    private static final String NOTIFICATION_CHANNEL = "TELESCOPE_TOUCH_SERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_START_SERVICE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                                getString(R.string.background_service), NotificationManager.IMPORTANCE_NONE);
                        channel.setLightColor(getColor(R.color.colorPrimary));
                        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                    }
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);
                    NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
                    bigTextStyle.setBigContentTitle(getString(R.string.app_name));
                    bigTextStyle.bigText("Telescope.Touch is running in the background");
                    builder.setStyle(bigTextStyle);
                    builder.setWhen(System.currentTimeMillis());
                    builder.setSmallIcon(R.drawable.stars_on);
                    builder.setPriority(Notification.PRIORITY_DEFAULT);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    Intent connectionIntent = new Intent(this, MainActivity.class);
                    connectionIntent.putExtra(MainActivity.ACTION, MainActivity.ACTION_CONNECT);
                    stackBuilder.addNextIntentWithParentStack(connectionIntent);
                    builder.setContentIntent(stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT));
                    if (connectionManager.isConnected()) {
                        Intent mountIntent = new Intent(this, MainActivity.class);
                        mountIntent.putExtra(MainActivity.ACTION, MainActivity.ACTION_MOUNT_CONTROL);
                        stackBuilder.addNextIntentWithParentStack(mountIntent);
                        builder.addAction(new NotificationCompat.Action(null, getString(R.string.mount),
                                stackBuilder.getPendingIntent(2, PendingIntent.FLAG_UPDATE_CURRENT)));

                        Intent disconnectIntent = new Intent(this, AppForegroundService.class);
                        disconnectIntent.setAction(ACTION_DISCONNECT);
                        builder.addAction(new NotificationCompat.Action(null, getString(R.string.disconnect),
                                PendingIntent.getService(this, 3, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT)));
                    } else {
                        Intent closeIntent = new Intent(this, AppForegroundService.class);
                        closeIntent.setAction(ACTION_EXIT);
                        builder.addAction(new NotificationCompat.Action(null, getString(R.string.exit),
                                PendingIntent.getService(this, 4, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT)));
                    }
                    startForeground(1, builder.build());
                    break;
                case ACTION_EXIT:
                    stopForeground(true);
                    stopSelf();
                    System.exit(0);
                    break;
                case ACTION_STOP_SERVICE:
                    stopForeground(true);
                    stopSelf();
                    break;
                case ACTION_DISCONNECT:
                    if (connectionManager.isConnected()) connectionManager.disconnect();
                    Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}