package com.velasanchez.proyectomovil.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.CalificationDriverActivity;
import com.velasanchez.proyectomovil.activities.client.MapClientActivity;
import com.velasanchez.proyectomovil.models.ClientBooking;
import com.velasanchez.proyectomovil.models.HistoryBooking;
import com.velasanchez.proyectomovil.providers.ClientBookingProvider;
import com.velasanchez.proyectomovil.providers.HistoryBookingProvider;

import java.util.Date;

public class CalificationClientActivity extends AppCompatActivity {

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private RatingBar mRatinBar;
    private Button mButtonCalification;
    private ClientBookingProvider mClienteBookingProvider;

    private HistoryBooking mHistoryBooking;

    private HistoryBookingProvider mHistoryBookingProvier;

    private String mExtraClientId;

    private float mCalification = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calification_client);

        mTextViewOrigin = findViewById(R.id.textViewOriginCalification);
        mTextViewDestination = findViewById(R.id.textViewDestinationCalification);
        mRatinBar = findViewById(R.id.ratingBarCalification);

        mClienteBookingProvider = new ClientBookingProvider();
        mHistoryBookingProvier = new HistoryBookingProvider();

        mExtraClientId = getIntent().getStringExtra("idClient");

        mButtonCalification = findViewById(R.id.btnCalification);

        mRatinBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            //DEVUELVE LO QUE ELIGIÓ EL USUARIO
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mCalification = rating;
            }
        });
        mButtonCalification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calificate();
            }
        });

        getClientBooking();
    }

    private void getClientBooking() {
        //SOLO TRAEREMOS LA INFO UNA VEZ
        mClienteBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Podria hacerlo como abajo, pero lo haré más fachero y jalaré toda la info de una
                    //String origen = snapshot.child("origin").getValue().toString();

                    //PARA HACER ESTO LOS CAMPOS DEL MODELO DEBEN LLAMARSE IGUAL A LOS DE LA BASE
                    ClientBooking clientBooking = snapshot.getValue(ClientBooking.class);
                    mTextViewOrigin.setText(clientBooking.getOrigin());
                    mTextViewDestination.setText(clientBooking.getDestination());
                    mHistoryBooking = new HistoryBooking(
                        clientBooking.getIdHistoryBooking(),
                            clientBooking.getIdClient(),
                            clientBooking.getIdDriver(),
                            clientBooking.getDestination(),
                            clientBooking.getOrigin(),
                            clientBooking.getTime(),
                            clientBooking.getKm(),
                            clientBooking.getStatus(),
                            clientBooking.getOriginLat(),
                            clientBooking.getOriginLng(),
                            clientBooking.getDestinationLat(),
                            clientBooking.getDestinationLng()
                    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void calificate() {
        if (mCalification > 0) {
            //ACTUALIZAMOS EL HISTORIAL EN LA BASE DE DATOS
            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimestamp(new Date().getTime());
            //COSA QUE SI NO EXISTE SE CREA Y SI EXISTE SE ACTUALIZA
            mHistoryBookingProvier.getHistoryBooking(mHistoryBooking.getIdHistoryBooking()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        mHistoryBookingProvier.updateCalificationClient(mHistoryBooking.getIdHistoryBooking(), mCalification).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this, "Muchas gracias por ayudar. Seguiremos mejorando para ti.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapDriverActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else {
                        mHistoryBookingProvier.create(mHistoryBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CalificationClientActivity.this, "Muchas gracias por ayudar. Seguiremos mejorando para ti.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CalificationClientActivity.this, MapDriverActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            Toast.makeText(this, "Debes ingresar la calificación", Toast.LENGTH_SHORT).show();
        }
    }
}