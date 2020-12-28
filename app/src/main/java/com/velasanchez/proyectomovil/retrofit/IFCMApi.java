package com.velasanchez.proyectomovil.retrofit;

import com.velasanchez.proyectomovil.models.FCMBody;
import com.velasanchez.proyectomovil.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAq53zkZc:APA91bFdg8AViBL9C53IAxOv6731H4Y50JdPXGRQ7OLYK2xS7RfWCr-KySewwEoJrOpECSMjZSfUyI5zOgRUJSCUWbahtPRF4Kgx-ne3Nye9kvcO5xclZii8L8l3WNlLI4moq5Y44ZbX"
    })
    //LA RUTA QUE NOS PERMITE ENVIAR NOTIFICACIONES
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
