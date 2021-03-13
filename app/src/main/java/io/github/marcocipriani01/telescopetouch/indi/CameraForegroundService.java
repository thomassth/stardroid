/*
 * Copyright 2020 Marco Cipriani (@marcocipriani01)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcocipriani01.telescopetouch.indi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import java.util.Collection;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.activities.MainActivity;

import static io.github.marcocipriani01.telescopetouch.TelescopeTouchApp.connectionManager;

public class CameraForegroundService extends Service
        implements INDICamera.CameraListener, ConnectionManager.ManagerListener {

    public static final String INDI_CAMERA_EXTRA = "indi_camera";
    public static final String ACTION_START_SERVICE = "ACTION_START_SERVICE";
    public static final String ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE";
    public static final String ACTION_STOP_CAPTURE = "ACTION_STOP_CAPTURE";
    private static final String NOTIFICATION_CHANNEL = "CCD_CAPTURE_SERVICE";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private INDICamera camera;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case ACTION_START_SERVICE:
                    camera = intent.getParcelableExtra(INDI_CAMERA_EXTRA);
                    if (camera == null) {
                        Toast.makeText(this, R.string.no_camera_available, Toast.LENGTH_SHORT).show();
                        stopForeground(true);
                        stopSelf();
                    } else {
                        camera.addListener(this);
                        start();
                    }
                    break;
                case ACTION_STOP_CAPTURE:
                    //TODO: stop capture
                    stopForeground(true);
                    stopSelf();
                    break;
                case ACTION_STOP_SERVICE:
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "CCD capture", NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(getColor(R.color.colorPrimary));
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
        notificationManager = NotificationManagerCompat.from(this);
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(camera.toString());
        bigTextStyle.bigText("CCD capture in progress");
        builder.setStyle(bigTextStyle);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.ccd_capture);
        builder.setPriority(Notification.PRIORITY_DEFAULT);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.putExtra(MainActivity.ACTION, MainActivity.ACTION_CCD_CAPTURE);
        stackBuilder.addNextIntentWithParentStack(appIntent);
        builder.setContentIntent(stackBuilder.getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent stopLoopIntent = new Intent(this, CameraForegroundService.class);
        stopLoopIntent.setAction(ACTION_STOP_CAPTURE);
        stopLoopIntent.putExtra(CameraForegroundService.INDI_CAMERA_EXTRA, camera);
        builder.addAction(new NotificationCompat.Action(null, "Stop capture",
                PendingIntent.getService(this, 2, stopLoopIntent, PendingIntent.FLAG_UPDATE_CURRENT)));

        int totalCaptures = camera.getLoopTotalCaptures();
        if (totalCaptures == 0) {
            builder.setProgress(0, 0, true);
        } else {
            builder.setProgress(totalCaptures, totalCaptures - camera.getRemainingCaptures(), false);
        }
        startForeground(1, builder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connectionManager.addManagerListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectionManager.removeManagerListener(this);
        if (camera != null) camera.removeListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnectionLost() {
        handler.post(() -> {
            Toast.makeText(this, R.string.connection_indi_lost, Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        });
    }

    @Override
    public void onCamerasListChange() {
        Collection<INDICamera> cameras = connectionManager.indiCameras.values();
        for (INDICamera camera : cameras) {
            if (camera == this.camera) return;
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onLoopProgressChange(int progress, int total) {
        builder.setProgress(total, progress, false);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onCameraLoopStop() {
        stopForeground(true);
        stopSelf();
    }
}