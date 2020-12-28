package com.velasanchez.proyectomovil.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.velasanchez.proyectomovil.models.Client;

import java.util.HashMap;
import java.util.Map;

public class ClientProvider {

    DatabaseReference mDatabase;

    public ClientProvider(){
        //Agregamos de una los child porque este será el provedor para tipo cliente
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Clients");
    }

    public Task<Void> create(Client client){
        //PARA NO MANDAR EL OBJECTO CLIENTE Y COJA EL ID QUE UTILIZAMOS EN EL CODIGO
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", client.getNombre());
        map.put("email", client.getEmail());

        return mDatabase.child(client.getId()).setValue(map);
    }

    public Task<Void> update(Client client){
        //PARA NO MANDAR EL OBJECTO CLIENTE Y COJA EL ID QUE UTILIZAMOS EN EL CODIGO
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", client.getNombre());
        map.put("image", client.getImage());

        return mDatabase.child(client.getId()).updateChildren(map);
    }

    public DatabaseReference getClient(String idClient) {
        return mDatabase.child(idClient);
    }
}
