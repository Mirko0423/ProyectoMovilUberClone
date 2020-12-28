package com.velasanchez.proyectomovil.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.RegisterActivity;
import com.velasanchez.proyectomovil.activities.driver.RegisterDriverActivity;
import com.velasanchez.proyectomovil.includes.MyToolbar;

public class SelectOptionAuthActivity extends AppCompatActivity {

    SharedPreferences mPref;

    Button mButtonGoToLogin;
    Button mButtonGoToRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_option_auth);

        MyToolbar.show(this, "Seleccionar opción", true);
        /*
        //CODIGO ANTES DE GENERAR NUESTRA CLASE PARA LA TOOLBAR
        mToolbar = findViewById(R.id.toolbar);
        //Recibe el actionbar
        setSupportActionBar(mToolbar);
        //Recibe el titulo
        getSupportActionBar().setTitle("Seleccionar opción");
        //Para habilitar o no el boton de retroceso
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        */

        //Inicializamos el preferences
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        mButtonGoToLogin = findViewById(R.id.btnGoToLogin);
        mButtonGoToRegister = findViewById(R.id.btnGoToRegister);
        mButtonGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
        mButtonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(SelectOptionAuthActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToRegister() {
        String typeUser = mPref.getString("user","");
        if (typeUser.equals("client")) {
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(SelectOptionAuthActivity.this, RegisterDriverActivity.class);
            startActivity(intent);
        }

    }
}