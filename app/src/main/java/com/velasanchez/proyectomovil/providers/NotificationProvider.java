package com.velasanchez.proyectomovil.providers;

import com.velasanchez.proyectomovil.models.FCMBody;
import com.velasanchez.proyectomovil.models.FCMResponse;
import com.velasanchez.proyectomovil.retrofit.IFCMApi;
import com.velasanchez.proyectomovil.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}
