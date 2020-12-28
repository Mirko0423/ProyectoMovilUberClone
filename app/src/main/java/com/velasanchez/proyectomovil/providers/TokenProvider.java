package com.velasanchez.proyectomovil.providers;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.velasanchez.proyectomovil.models.Token;

public class TokenProvider {

    DatabaseReference mDataBase;

    public TokenProvider() {
        mDataBase = FirebaseDatabase.getInstance().getReference().child("Tokens");
    }

    public void create(final String idUser) {
        //PARA QUE NO INSERTE DATOS VACIOS
        if (idUser == null) return;
        //CON LA INSTANCIA DEL USUARIO ACTIVA, LE GENERA UN TOKEN
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token token = new Token(instanceIdResult.getToken());
                mDataBase.child(idUser).setValue(token);
            }
        });
    }

    public DatabaseReference getToken(String idUser) {
        return mDataBase.child(idUser);
    }
}
