package com.velasanchez.proyectomovil.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.velasanchez.proyectomovil.models.ClientBooking;

import java.util.HashMap;
import java.util.Map;

//Manejar la reserva del cliente
public class ClientBookingProvider {

    private DatabaseReference mDatabase;

    public ClientBookingProvider() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("ClientBooking");
    }

    public Task<Void> create(ClientBooking clientBooking) {
        return mDatabase.child(clientBooking.getIdClient()).setValue(clientBooking);
    }

    //ACTUALIZA EL ESTADO DEL VIAJE
    public Task<Void> updateStatus(String idClientBooking, String status) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    //ACTUALIZA EL ESTADO DEL VIAJE
    public Task<Void> updateIdHistoryBooking(String idClientBooking) {
        //CREAMOS UN IDENTIFICADOR UNICO
        String idPush = mDatabase.push().getKey();
        Map<String, Object> map = new HashMap<>();
        map.put("idHistoryBooking", idPush);
        return mDatabase.child(idClientBooking).updateChildren(map);
    }

    //METODO PARA HACER REFERENCIA AL STATUS Y SABER CUANDO EL CONDUCTOR ACEPTA LA SOLICITUD
    //EL ID COMO PROGRAMAMOS ES EL MISMO DEL CLIENTE Y LOS CHILD HACE QUE SE APUNTE A SE LUGAR
    public DatabaseReference getStatus(String idClientBooking) {
        return mDatabase.child(idClientBooking).child("status");
    }

    //Me retornará todo el nodo ClientBooking
    public DatabaseReference getClientBooking(String idClientBooking) {
        return mDatabase.child(idClientBooking);
    }

    public  Task<Void> delete(String idClientBooking) {
        //PARA ELIMINAR CLIENTBOOKING
        return mDatabase.child(idClientBooking).removeValue();
    }
}
