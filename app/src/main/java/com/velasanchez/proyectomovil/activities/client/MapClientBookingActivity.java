package com.velasanchez.proyectomovil.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.driver.MapDriverBookingActivity;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientBookingProvider;
import com.velasanchez.proyectomovil.providers.DriverProvider;
import com.velasanchez.proyectomovil.providers.GeofireProvider;
import com.velasanchez.proyectomovil.providers.GoogleApiProvider;
import com.velasanchez.proyectomovil.providers.TokenProvider;
import com.velasanchez.proyectomovil.retrofit.IFCMApi;
import com.velasanchez.proyectomovil.util.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    AuthProvider mAuthProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private GeofireProvider mGeofireProvider;

    //PROPIEDAD PARA PONER EL ICONO EN EL MAPA
    private Marker mMarkerDriver;

    //Validar para que el callback no se actualice cada que se mueve la persona
    private boolean mIsFirstTime = true;

    private PlacesClient mPlaces;

    //ALMACENARA EL NOMBRE DEL ORIGEN SELECCIONADO
    private String mOrigin;
    //ALMACENARA LA LAT Y LONG DEL ORIGEN SELECCIONADO
    private LatLng mOriginLatLng;
    //ALMACENARA EL NOMBRE DEL DESTINO SELECCIONADO
    private String mDestination;
    //ALMACENARA LA LAT Y LONG DEL DESTINO SELECCIONADO
    private LatLng mDestinationLatLng;

    private TokenProvider mTokenProvider;

    private TextView mtextViewDriverBooking;
    private TextView mtextViewEmailDriverBooking;
    private TextView mtextViewOriginDriverBooking;
    private TextView mtextViewDestinationDriverBooking;
    private TextView mTextViewStatusBooking;
    private ImageView mImageViewBooking;

    private ClientBookingProvider mClientBookingProvider;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolyLineOptions;

    //POSICION DEL CONDUCTOR
    private LatLng mDriverLatLng;

    private DriverProvider mDriverProvider;

    private ValueEventListener mListener;

    private String mIdDriver;

    private ValueEventListener mListenerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);

        //Instancio mi objeto provider
        mAuthProvider = new AuthProvider();

        mGeofireProvider = new GeofireProvider("drivers_working");

        mTokenProvider = new TokenProvider();

        mClientBookingProvider = new ClientBookingProvider();

        mDriverProvider = new DriverProvider();

        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Este metodo recibe un callback y nuestra actividad la implementa
        mMapFragment.getMapAsync(this);


        if (!Places.isInitialized()) {
            //Utilizamos el contexto y el api key como parametros
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mtextViewDriverBooking = findViewById(R.id.textViewDriverBooking);
        mtextViewEmailDriverBooking = findViewById(R.id.textViewEmailDriverBooking);
        mtextViewOriginDriverBooking = findViewById(R.id.textViewOriginDriverBooking);
        mtextViewDestinationDriverBooking = findViewById(R.id.textViewDestinationDriverBooking);
        mTextViewStatusBooking = findViewById(R.id.textViewStatusBooking);
        mImageViewBooking = findViewById(R.id.imageViewDriverBooking);

        getStatus();
        getClientBooking();
    }

    private void getStatus() {
        mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.getValue().toString();
                    if (status.equals("accept")){
                        mTextViewStatusBooking.setText("Estado: Aceptado");
                    }
                    if (status.equals("start")) {
                        mTextViewStatusBooking.setText("Estado: Iniciado");
                        startBooking();
                    }
                    else if(status.equals("finish")) {
                        mTextViewStatusBooking.setText("Estado: Finalizado");
                        finishBooking();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriverActivity.class);
        startActivity(intent);
        finish();
    }

    private void startBooking() {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.pinverde)));
        drawRoute(mDestinationLatLng);
    }

    //DESPUES DE CERRARSE LA PANTALLA DEJE DE ESCUCHAR
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mGeofireProvider.getDriverLocation(mIdDriver).removeEventListener(mListener);
        }
        if (mListenerStatus != null) {
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListenerStatus);
        }
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String destination = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    String idDriver = snapshot.child("idDriver").getValue().toString();
                    mIdDriver = idDriver;
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());

                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinationLat, destinationLng);
                    mtextViewOriginDriverBooking.setText("Punto de recogida: "+origin);
                    mtextViewDestinationDriverBooking.setText("Destino: "+destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.pinrojo)));
                    getDriver(idDriver);
                    getDriverLocation(idDriver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriver(String idDriver) {
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("nombre").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")) {
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(MapClientBookingActivity.this).load(image).into(mImageViewBooking);
                    }
                    mtextViewDriverBooking.setText(name);
                    mtextViewEmailDriverBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDriverLocation(String idDriver) {
        //ESCUCHAR EN TIEMPO REAL
        mListener = mGeofireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                    double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                    mDriverLatLng = new LatLng(lat,lng);
                    //PARA EVITAR REDIBUJADAS
                    if (mMarkerDriver != null) {
                        mMarkerDriver.remove();
                    }
                    mMarkerDriver = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(lat, lng))
                            .title("Tu conductor")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
                    //TRAZAR LA RUTA UNA SOLA VEZ
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mDriverLatLng)
                                        .zoom(17f)
                                        .build()
                        ));
                        drawRoute(mOriginLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        //TRAZAR LA RUTA DESDE LA POSICIÓN DEL CONDUCTOR(DRIVER WORKING) AL LUGAR DE RECOGIDA
        mGoogleApiProvider.getDirections(mDriverLatLng, latLng).enqueue(new Callback<String>() {
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
                    //UTILIZAMOS UN METODO DE INTERNET MODIFICADO PARA DECODIFICAR
                    mPolylineList = DecodePoints.decodePoly(points);
                    //ESTABLECEMOS LAS PROPIEDADES
                    mPolyLineOptions = new PolylineOptions();
                    mPolyLineOptions.color(Color.DKGRAY);
                    mPolyLineOptions.width(13f);
                    mPolyLineOptions.startCap(new SquareCap());
                    mPolyLineOptions.jointType(JointType.ROUND);
                    mPolyLineOptions.addAll(mPolylineList);
                    //AGREGAMOS AL MAPA
                    mMap.addPolyline(mPolyLineOptions);

                    //OBTENDREMOS LA DISTANCIA Y EL TIEMPO
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
}