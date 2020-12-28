package com.velasanchez.proyectomovil.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.velasanchez.proyectomovil.activities.driver.MapDriverBookingActivity;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientBookingProvider;
import com.velasanchez.proyectomovil.providers.GeofireProvider;

public class AcceptReceiver extends BroadcastReceiver {

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    //METODO A EJECUTAR CUANDO ACEPTEMOS LA ACCIÓN EN LA NOTIFIACIÓN
    @Override
    public void onReceive(Context context, Intent intent) {
        //De aquí obtendremos el id de la sesión
        mAuthProvider = new AuthProvider();
        //ESTO LO HACEMOS PARA BORRAR AL CONDUCTOR DE LA LISTA DE CONDUCTORES ACTIVOS AL ACEPTAR UNA SOLICITUD DE TAXI
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getId());

        //Para recibir el id que obtiene en el MyFrisebaseMessagingClient
        String idClient = intent.getExtras().getString("idClient");
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.updateStatus(idClient, "accept");

        //AHORA HACER QUE DESAPAREZCA LA NOTIFICACIÓN CUANDO CAMBIA AL ESTADO ACEPTADA
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //RECIBE EL ID QUE LE PUSIMOS A LA NOTIFICACIÓN, EN ESTE CASO LAS FACHERAS TIENEN 2
        manager.cancel(2);

        //PARA DIRIGIR A UNA NUEVA ACTIVIDAD CUANDO EL CONDUCTO PRESIONA ACEPTAR
        Intent intent1 = new Intent(context, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient",idClient);
        context.startActivity(intent1);
    }
}
