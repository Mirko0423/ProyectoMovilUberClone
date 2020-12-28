package com.velasanchez.proyectomovil.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    //URL ES DONDE VAMOS A REALIZAR LA PETICIÓN
    public static Retrofit getClient(String url) {
            Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(url)
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .build();
        return retrofit;
    }

    //METODO PARA ENVIAR PETICIÓN HTTP A UN SERVICIO DE FIREBASE PARA ENVIAR NOTIFICACIONES DE DISPOSITIVO A DISPOSITIVO
    public static Retrofit getClientObject(String url) {
             Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        return retrofit;
    }
}
