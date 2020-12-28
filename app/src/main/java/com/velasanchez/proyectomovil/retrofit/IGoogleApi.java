package com.velasanchez.proyectomovil.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleApi {

    //INTERFAZ QUE SERÁ LA QUE SE COMUNICARA CON LA PETICIÓN HTTP CON GOOGLE MEDIANTE EL URL
    @GET
    Call<String> getDirections(@Url String url);
}
