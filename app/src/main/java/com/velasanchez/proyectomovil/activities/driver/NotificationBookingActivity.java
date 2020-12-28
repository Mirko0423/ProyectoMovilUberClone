package com.velasanchez.proyectomovil.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientBookingProvider;
import com.velasanchez.proyectomovil.providers.GeofireProvider;

public class NotificationBookingActivity extends AppCompatActivity {

    private TextView mTextViewDestination;
    private TextView mTextViewOrigin;
    private TextView mTextViewMin;
    private TextView mTextViewDistance;
    private TextView mTextViewCounter;
    private Button mButtonAccept;
    private Button mButtonCancel;

    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;

    private String mExtraIdClient;
    private String mExtraOrigin;
    private String mExtraDestination;
    private String mExtraMin;
    private String mExtraDistance;

    private MediaPlayer mMediaPlayer;

    private Handler mHandler;
    private int mCounter = 10;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCounter = mCounter - 1;
            mTextViewCounter.setText(String.valueOf(mCounter));
            if (mCounter > 0) {
                initTimer();
            }
            else {
                cancelBooking();
            }
        }
    };
    private ValueEventListener mListener;

    private void initTimer() {
        mHandler =  new Handler();
        //INICIALIZAR CADA 1000 MILISEGUNDOS = 1S
        mHandler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_booking);

        mTextViewDestination = findViewById(R.id.textViewDestination);
        mTextViewOrigin = findViewById(R.id.textViewOrigin);
        mTextViewMin = findViewById(R.id.textViewMin);
        mTextViewDistance = findViewById(R.id.textViewDistance);
        mTextViewCounter = findViewById(R.id.textViewCounter);

        mExtraIdClient = getIntent().getStringExtra("idClient");
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraMin = getIntent().getStringExtra("min");
        mExtraDistance = getIntent().getStringExtra("distance");

        mTextViewDestination.setText(mExtraDestination);
        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewMin.setText(mExtraMin);
        mTextViewDistance.setText(mExtraDistance);

        mMediaPlayer = MediaPlayer.create(this, R.raw.taxi_whistle);
        mMediaPlayer.setLooping(true);

        mClientBookingProvider = new ClientBookingProvider();

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        initTimer();
        checkIfClientCancelBooking();

        mButtonAccept = findViewById(R.id.btnAcceptBooking);
        mButtonCancel = findViewById(R.id.btnCancelBooking);

        mButtonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptBooking();
            }
        });

        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBooking();
            }
        });
    }

    private void checkIfClientCancelBooking() {
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //SI NO EXISTIERA LA INFO DE BOOKING
                if (!snapshot.exists()){
                    Toast.makeText(NotificationBookingActivity.this, "El cliente ha cancelado el viaje", Toast.LENGTH_LONG).show();
                    //PARA QUE FINALICE EL TEMPORIZADOR
                    if (mHandler != null) mHandler.removeCallbacks(runnable);
                    Intent intent = new Intent(NotificationBookingActivity.this, MapDriverActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void cancelBooking() {
        //Para que el contador no siga corriendo
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        mClientBookingProvider.updateStatus(mExtraIdClient, "cancel");

        //AHORA HACER QUE DESAPAREZCA LA NOTIFICACIÓN CUANDO CAMBIA AL ESTADO ACEPTADA
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //RECIBE EL ID QUE LE PUSIMOS A LA NOTIFICACIÓN, EN ESTE CASO LAS FACHERAS TIENEN 2
        manager.cancel(2);
        Intent intent = new Intent(NotificationBookingActivity.this, MapDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private void acceptBooking() {
        //Para que el contador no siga corriendo
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        //De aquí obtendremos el id de la sesión
        mAuthProvider = new AuthProvider();
        //ESTO LO HACEMOS PARA BORRAR AL CONDUCTOR DE LA LISTA DE CONDUCTORES ACTIVOS AL ACEPTAR UNA SOLICITUD DE TAXI
        mGeofireProvider = new GeofireProvider("active_drivers");
        mGeofireProvider.removeLocation(mAuthProvider.getId());

        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.updateStatus(mExtraIdClient, "accept");

        //AHORA HACER QUE DESAPAREZCA LA NOTIFICACIÓN CUANDO CAMBIA AL ESTADO ACEPTADA
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //RECIBE EL ID QUE LE PUSIMOS A LA NOTIFICACIÓN, EN ESTE CASO LAS FACHERAS TIENEN 2
        manager.cancel(2);

        //PARA DIRIGIR A UNA NUEVA ACTIVIDAD CUANDO EL CONDUCTO PRESIONA ACEPTAR
        Intent intent1 = new Intent(NotificationBookingActivity.this, MapDriverBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra("idClient", mExtraIdClient);
        startActivity(intent1);
    }

    //METODOS DEL CICLO DE VIDA, VALIDACIONES


    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
    }

    //CUANDO SE MINIMIZA LA APP
    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.release();
            }
        }
    }

    //SE EJECUTA CUANDO TODA LA ACTIVIDAD HA SIDO CREADA
    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null){
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Para que el contador no siga corriendo
        if (mHandler != null) mHandler.removeCallbacks(runnable);

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        if (mListener != null) {
            mClientBookingProvider.getClientBooking(mExtraIdClient).removeEventListener(mListener);
        }
    }
}