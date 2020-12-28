package com.velasanchez.proyectomovil.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.velasanchez.proyectomovil.providers.ClientBookingProvider;

public class CancelReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;

    //METODO A EJECUTAR CUANDO ACEPTEMOS LA ACCIÓN EN LA NOTIFIACIÓN
    @Override
    public void onReceive(Context context, Intent intent) {
        //Para recibir el id que obtiene en el MyFrisebaseMessagingClient
        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.updateStatus(idClient, "cancel");

        //AHORA HACER QUE DESAPAREZCA LA NOTIFICACIÓN CUANDO CAMBIA AL ESTADO ACEPTADA
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //RECIBE EL ID QUE LE PUSIMOS A LA NOTIFICACIÓN, EN ESTE CASO LAS FACHERAS TIENEN 2
        manager.cancel(2);
    }
}
