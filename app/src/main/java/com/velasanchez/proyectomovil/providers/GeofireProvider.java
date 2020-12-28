package com.velasanchez.proyectomovil.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {
    private DatabaseReference mDatabase;
    private GeoFire mGeoFire;

    public GeofireProvider(String reference) {
        //AQUÍ CREAMOS EL NUEVO NODO
        mDatabase = FirebaseDatabase.getInstance().getReference().child(reference);
        //RECIBE LA REFERENCIA DE NUESTRA BASE DE DATOS
        mGeoFire = new GeoFire(mDatabase);
    }

    //GUARDAR LA LOCALIZACIÓN EN FIREBASE
    public void saveLocation(String idDriver, LatLng latLng) {
        mGeoFire.setLocation(idDriver, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    //BORRAR LA LOCALIZACIÓN
    public  void removeLocation(String idDriver) {
        mGeoFire.removeLocation(idDriver);
    }

    //Obtener los conductores disponibles
    public GeoQuery getActiveDrivers(LatLng latLng, double radius) {
        //Recibe una localización y un radio de hasta donde apareceran los conductores en km
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    //Metodo para saber si un conductor se conecto al nodo conductores activos
    public DatabaseReference isDriverWorking(String idDriver) {
        return FirebaseDatabase.getInstance().getReference().child("drivers_working").child(idDriver);
    }

    public DatabaseReference getDriverLocation(String idDriver) {
        return mDatabase.child(idDriver).child("l");
    }
}
