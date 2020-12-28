package com.velasanchez.proyectomovil.activities.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.HistoryBookingClientActivity;
import com.velasanchez.proyectomovil.adapters.HistoryBookingClientAdapter;
import com.velasanchez.proyectomovil.adapters.HistoryBookingDriverAdapter;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.models.HistoryBooking;
import com.velasanchez.proyectomovil.providers.AuthProvider;

public class HistoryBookingDriverActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private HistoryBookingDriverAdapter mAdapter;
    private AuthProvider mAuthProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_driver);

        MyToolbar.show(this, "Historial de viajes", true);

        mRecyclerView = findViewById(R.id.recyclerViewHistoryBooking);

        //NUESTRO RECYCLER VIEW NECESITA ESTA CONFIGURACION PARA MOSTRAR LA INFORMACIÓN OBTENIDA DESDE FIREBASE
        //linear lo mostrara en vertical uno debajo de otro
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuthProvider = new AuthProvider();
        //CONSULTA PARA OBTENER LOS HISTORIALES DE ESE CLIENTE
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("HistoryBooking")
                .orderByChild("idDriver")
                .equalTo(mAuthProvider.getId());
        //RECIBE COMO PARAMETRO LAS OPCIONES Y EL CONTEXTO
        //EL SET QUERY LA CONSULTA Y DE QUE TIPO SERA LA CLASE QUE NOS RETORNE
        FirebaseRecyclerOptions<HistoryBooking> options = new FirebaseRecyclerOptions.Builder<HistoryBooking>()
                .setQuery(query, HistoryBooking.class)
                .build();
        mAdapter = new HistoryBookingDriverAdapter(options, HistoryBookingDriverActivity.this);

        mRecyclerView.setAdapter(mAdapter);
        //PARA QUE ESCUCHE LOS CAMBIOS
        mAdapter.startListening();
    }

    //AL MINIMIZAR LA APP DEJE DE ESCUCHAR
    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}