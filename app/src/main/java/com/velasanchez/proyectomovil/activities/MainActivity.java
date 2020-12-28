package com.velasanchez.proyectomovil.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.MapClientActivity;
import com.velasanchez.proyectomovil.activities.driver.MapDriverActivity;
import com.velasanchez.proyectomovil.providers.AuthProvider;

public class MainActivity extends AppCompatActivity {

    Button mButtonCliente;
    Button mButtonConductor;
    /*Variable que usaremos para manejar si es cliente o conductor, se guarda hasta borrar la aplicación o elimianarla*/
    SharedPreferences mPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Inicialización*/
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        final SharedPreferences.Editor editor = mPref.edit();

        /*Referenciamos la variable al id del xml*/
        mButtonCliente = findViewById(R.id.btnCliente);
        mButtonConductor = findViewById(R.id.btnConductor);

        /*Metodo para hacer referencia al click, el parametro con ctrl + espacio*/
        mButtonCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            /*Aquí se programa que hará el click*/
            public void onClick(View v) {
                /*Identificador llamado user, de tipo client de cliente*/
                editor.putString("user", "client");
                editor.apply();
                goToSelectAuth();
            }
        });

        mButtonConductor.setOnClickListener(new View.OnClickListener() {
            @Override
            /*Aquí se programa que hará el click*/
            public void onClick(View v) {
                editor.putString("user", "driver");
                /*Metodo para que el editor guarde el valor*/
                editor.apply();
                goToSelectAuth();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Obtenemos si es que existe una sesión activa
        if (FirebaseAuth.getInstance().getCurrentUser()!=null) {
            //Según que tipo de cliente sea redireccionamos al mapa que le toca
            String user = mPref.getString("user", "");
            if (user.equals("client")) {
                //Para redireccionar al mapa
                Intent intent = new Intent(MainActivity.this, MapClientActivity.class);
                //Asegurarnos que no pueda regresar al registro
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            else {
                //Para redireccionar al mapa
                Intent intent = new Intent(MainActivity.this, MapDriverActivity.class);
                //Asegurarnos que no pueda regresar al registro
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }

    /*Alt+enter, atajo para crear un metodo*/
    private void goToSelectAuth() {
        /*Propiedad intent que recive el contexto donde estamos y a donde queremos ir*/
        Intent intent = new Intent(MainActivity.this, SelectOptionAuthActivity.class);
        /*Inicia el intent*/
        startActivity(intent);
    }
}