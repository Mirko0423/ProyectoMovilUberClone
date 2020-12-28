package com.velasanchez.proyectomovil.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.velasanchez.proyectomovil.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.velasanchez.proyectomovil";
    private static final String CHANNEL_NAME = "CreativeTaxi";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        //SE VALIDA QUE ESTEMOS EN UN SISTEMA OPERATIVO OREO O SUPEIOR PQ SINO SALE ERROR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }
    }

    //ES NECESARIO CREA UN CANAL DE NOTIFICACIONES DESDE LAS VERSIONES ANDROID OREO A SUPERIOR
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel notificationChannel = new
                NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        //PROPIEDADES
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {
        //SINO ESTÁ INSTANCIADO
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    //CREA LA API EN VERSIONES 26 O SUPERIOR
    public Notification.Builder getNotification(String title, String body, PendingIntent intent, Uri soundUri) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.taxi_message)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    //NOTIFICACIONES CON BOTONES
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificationActions(String title,
                                                       String body,
                                                       Uri soundUri,
                                                       Notification.Action acceptAction,
                                                       Notification.Action cancelAction) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.taxi_message)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    //CREA LA API EN VERSIONES INFERIORES
    public NotificationCompat.Builder getNotificationOldApi(String title, String body, PendingIntent intent, Uri soundUri) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.taxi_message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    //NOTIFICACIONES CON BOTONES
    public NotificationCompat.Builder getNotificationOldApiActions(String title,
                                                                   String body,
                                                                   Uri soundUri,
                                                                   NotificationCompat.Action acceptAction,
                                                                   NotificationCompat.Action cancelAction) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setSmallIcon(R.drawable.taxi_message)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

}
