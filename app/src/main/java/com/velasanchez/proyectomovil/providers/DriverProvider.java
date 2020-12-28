package com.velasanchez.proyectomovil.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.velasanchez.proyectomovil.models.Client;
import com.velasanchez.proyectomovil.models.Driver;

import java.util.HashMap;
import java.util.Map;

public class DriverProvider {

    DatabaseReference mDatabase;

    public DriverProvider(){
        //Agregamos de una los child porque este será el provedor para tipo cliente
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
    }

    public Task<Void> create(Driver driver){
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", driver.getNombre());
        map.put("email", driver.getEmail());
        map.put("marcaVehiculo", driver.getMarcaVehiculo());
        map.put("placaVehiculo", driver.getPlacaVehiculo());

        return mDatabase.child(driver.getId()).setValue(map);
    }

    public Task<Void> update(Driver driver){
        //PARA NO MANDAR EL OBJECTO CLIENTE Y COJA EL ID QUE UTILIZAMOS EN EL CODIGO
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", driver.getNombre());
        map.put("marcaVehiculo", driver.getMarcaVehiculo());
        map.put("placaVehiculo", driver.getPlacaVehiculo());
        map.put("image", driver.getImage());

        return mDatabase.child(driver.getId()).updateChildren(map);
    }

    public DatabaseReference getDriver(String idDriver) {
        return mDatabase.child(idDriver);
    }
}
