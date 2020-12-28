package com.velasanchez.proyectomovil.includes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.velasanchez.proyectomovil.R;

public class MyToolbar {
    //Parametros: appCompatActivity(Contexto), titulo, boton o no de retroceso
    public static void show(AppCompatActivity activity, String title, boolean upButton){
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setTitle(title);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }
}
