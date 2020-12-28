package com.velasanchez.proyectomovil.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.models.ClientBooking;
import com.velasanchez.proyectomovil.models.FCMBody;
import com.velasanchez.proyectomovil.models.FCMResponse;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientBookingProvider;
import com.velasanchez.proyectomovil.providers.GeofireProvider;
import com.velasanchez.proyectomovil.providers.GoogleApiProvider;
import com.velasanchez.proyectomovil.providers.NotificationProvider;
import com.velasanchez.proyectomovil.providers.TokenProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;

    private GeofireProvider mGeoFireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private double mRadius = 0.1;

    //BANDERA PARA SABER SI YA HAY CONDUCTOR ENCONTRADO
    private boolean mDriverFound = false;
    //ALMACENADA ID DEL CONDUCTOR ENCONTRADO
    private String mIdDriverFound = "";
    //ALMACENARA LA POSICIÓN
    private LatLng mDriverFoundLatLng;

    private NotificationProvider mNotificationProvider;

    private TokenProvider mTokenProvider;

    private ClientBookingProvider mClientBookingProvider;

    private AuthProvider mAuthProvider;

    private String mExtraOrigin;
    private String mExtraDestination;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;

    private GoogleApiProvider mGoogleApiProvider;

    private ValueEventListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        mAnimation = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);
        mAnimation.playAnimation();

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat",0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng",0);
        mExtraDestination = getIntent().getStringExtra("destination");
        mExtraOrigin = getIntent().getStringExtra("origin");

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);

        mGeoFireProvider = new GeofireProvider("active_drivers");

        mNotificationProvider = new NotificationProvider();

        mTokenProvider = new TokenProvider();

        mClientBookingProvider = new ClientBookingProvider();

        mAuthProvider = new AuthProvider();

        mGoogleApiProvider = new GoogleApiProvider(RequestDriverActivity.this);

        mButtonCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRequest();
            }
        });
        getClosestDriver();
    }

    private void cancelRequest() {
        //El on success para saber si se ejecutó correctamente
        mClientBookingProvider.delete(mAuthProvider.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Enviaremos otra notificación cuando se haya eliminado la info del clientbooking
                sendNotificationCancel();
            }
        });
    }

    //METODO PARA OBTENER EL CONDUCTOR MÁS CERCANO
    private void getClosestDriver() {
        mGeoFireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            //SE EJECUTA CUANDO UBICA A UN CONDUCTOR DISPONIBLE, DEVOLVERA EL ID DEL CONDUCTOR Y EL OBJ LOCATION QUE ES LA POSICIÓN
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!mDriverFound) {
                    mDriverFound = true;
                    mIdDriverFound = key;
                    mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                    mTextViewLookingFor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");
                    //DESPUES DE ENCONTRAR AL CONDUCTOR MÁS CERCANO
                    createClientBooking();
                    Log.d("DRIVER", "ID: " + mIdDriverFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            //SE EJECUTA AL ACABAR EL METODO DE BUSQUEDA DE CONDUCTORES EN EL RADIO DE 0.1KM
            @Override
            public void onGeoQueryReady() {
                if (!mDriverFound) {
                    mRadius = mRadius + 0.1f;
                    //SI NO ENCONTRÓ NINGUN CONDUCTOR
                    if (mRadius > 5) {
                        mTextViewLookingFor.setText("NO SE ENCUENTRAN CONDUCTORES DISPONIBLES\nEN ESTE MOMENTO");
                        Toast.makeText(RequestDriverActivity.this, "No se pudo encontrar ningun conductor disponible", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //SINO SIGUE EJECUTANDOSE CON UN RADIO MAYOR
                    else {
                        getClosestDriver();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    //Peticion a la api de googlemaps
    private void createClientBooking() {
        //LA DISTANCIA DESDE EL ORIGEN AL CONDUCTOR MÁS CERCANO
        mGoogleApiProvider.getDirections(mOriginLatLng, mDriverFoundLatLng).enqueue(new Callback<String>() {
            //RESPUESTA DEL SERVIDOR
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    //ESTE OBTIENE EL PARAMETRO DEL PUNTO DEL TEXTO QUE NOS DEVUELVE LA PETICIÓN, EN ESTE CASO QUEREMOS ROUTES
                    //PQ DE AHÍ SACAREMOS LA INFORMACIÓN PARA TRAZAR LA RUTA
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    //DEL ARRAY OBTENGO EL ROUTE
                    JSONObject route = jsonArray.getJSONObject(0);
                    //DEL ROUTE OBTENGO LA INFORMACIÓN DEL POLIGONO
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    //DEL POLIGONO EL CODIGO DE LOS PUNTOS PERO ESTÁN CODIFICADOS
                    String points = polylines.getString("points");

                    //OBTENDREMOS LA DISTANCIA Y EL TIEMPO
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");

                    sendNotification(durationText, distanceText);
                } catch (Exception e) {
                    Log.e("Error", "Error encontrado: " + e.getMessage());
                }
            }

            //EN CASO FALLE LA PETICIÓN
            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void sendNotificationCancel() {
        //NECESITAMOS EL TO QUE VENDRIA SIENDO EL TOKEN DEL DATABASE DEL CONDUCTOR MAS CERCANO
        //ESE METODO OBTIENE EL VALOR UNA VEZ
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            //El DataSnapshot contiene la información de todo el nodo
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //VALIDAMOS EN CASO NO OBTENGA UN TOKEN
                if (snapshot.exists()) {
                    //OBTENDREMOS EL TOKEN
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "VIAJE CANCELADO" );
                    map.put("body",
                            "El cliente canceló la solicitud");
                    //4500s es un valor predeterminado segun la documentación en el modelo vemos para que sirve
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s" , map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1){
                                    Toast.makeText(RequestDriverActivity.this, "La solicitud ha sido cancelada", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RequestDriverActivity.this, MapClientActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Toast.makeText(RequestDriverActivity.this, "La notificación no ha podido ser enviada", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(RequestDriverActivity.this, "La notificación no ha podido ser enviada", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("ERROR", "Error "+t.getMessage());

                        }
                    });
                }
                else {
                    Toast.makeText(RequestDriverActivity.this, "La notificación no ha podido ser enviada porque el conductor no posee un token de sesión", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //Le puse tiempo y kilometros a la notificación para que se vea más fachera
    private void sendNotification(final String time, final  String km) {
        //NECESITAMOS EL TO QUE VENDRIA SIENDO EL TOKEN DEL DATABASE DEL CONDUCTOR MAS CERCANO
        //ESE METODO OBTIENE EL VALOR UNA VEZ
        mTokenProvider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            //El DataSnapshot contiene la información de todo el nodo
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //VALIDAMOS EN CASO NO OBTENGA UN TOKEN
                if (snapshot.exists()) {
                    //OBTENDREMOS EL TOKEN
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "SOLICITUD DE SERVICIO A " + time + " DE TU POSICIÓN" );
                    map.put("body",
                            "Un cliente está solicitando el servicio a una distancia de " + km + "\n" +
                            "Recoger en: " + mExtraOrigin + "\n" +
                            "Destino: " + mExtraDestination);
                    map.put("idClient", mAuthProvider.getId());
                    map.put("origin", mExtraOrigin);
                    map.put("destination", mExtraDestination);
                    map.put("min", time);
                    map.put("distance",km);
                    //4500s es un valor predeterminado segun la documentación en el modelo vemos para que sirve
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s" , map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1){
                                    //YA TENEMOS ESTOS VALORES EN EL METODO (TIME Y KM)
                                    ClientBooking clientBooking = new ClientBooking(
                                            mAuthProvider.getId(),
                                            mIdDriverFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    );
                                    //EL METODO ADDONSUCESS ES PARA SABER SI SE CREO NICE EN LA BD
                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Toast.makeText(RequestDriverActivity.this, "La petición se creó correctamente", Toast.LENGTH_SHORT).show();
                                            checkStatusClientBooking();
                                        }
                                    });
                                    //YA PUSE EL DE ARRIBA
                                    //Toast.makeText(RequestDriverActivity.this, "La notificación ha sido enviada correctamente", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(RequestDriverActivity.this, "La notificación no ha podido ser enviada", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(RequestDriverActivity.this, "La notificación no ha podido ser enviada", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("ERROR", "Error "+t.getMessage());

                        }
                    });
                }
                else {
                    Toast.makeText(RequestDriverActivity.this, "La notificación no ha podido ser enviada porque el conductor no posee un token de sesión", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkStatusClientBooking() {
        //ESTE METODO AL CONTRATIO DEL addListenerForSingleValueEvent QUE UTILIZAMOS EN EL SendNotification que se uso para obtener toke
        //este escuchara en tiempo real
        mListener = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Obtenemos el valor, ya no necesitamos despues del getValue el child para apuntar como lo hicimos en el token
                    //Pq en el metodo get status ya hicimos eso
                    String status = snapshot.getValue().toString();
                    if (status.equals("accept")) {
                        Intent intent = new Intent(RequestDriverActivity.this, MapClientBookingActivity.class);
                        startActivity(intent);
                        //Para que la actividad finaliza y no podamos ir atras
                        finish();
                    }
                    else if (status.equals("cancel")) {
                        Toast.makeText(RequestDriverActivity.this, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RequestDriverActivity.this, MapClientActivity.class);
                        startActivity(intent);
                        //Para que la actividad finaliza y no podamos ir atras
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //SE EJECUTA AL FINALIZAR UNA ACTIVIDAD, DETIENE TODO
    //UTILIZAMOS ESTO PARA DESTRUIR O DETENER LA ESCUCHA DEL addValueEventListener para que no se la pase escuchando despues de ya haber
    //Obtenido un resultado ya que saturaria la aplicación cada vez que la aplicación llame a ese metodo y nunca acabarian
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }
}