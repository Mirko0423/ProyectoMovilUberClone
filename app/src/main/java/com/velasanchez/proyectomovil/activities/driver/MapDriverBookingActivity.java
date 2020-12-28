package com.velasanchez.proyectomovil.activities.driver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.DetailRequestActivity;
import com.velasanchez.proyectomovil.activities.client.RequestDriverActivity;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.models.Client;
import com.velasanchez.proyectomovil.models.ClientBooking;
import com.velasanchez.proyectomovil.models.FCMBody;
import com.velasanchez.proyectomovil.models.FCMResponse;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientBookingProvider;
import com.velasanchez.proyectomovil.providers.ClientProvider;
import com.velasanchez.proyectomovil.providers.GeofireProvider;
import com.velasanchez.proyectomovil.providers.GoogleApiProvider;
import com.velasanchez.proyectomovil.providers.NotificationProvider;
import com.velasanchez.proyectomovil.providers.TokenProvider;
import com.velasanchez.proyectomovil.util.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriverBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    AuthProvider mAuthProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private GeofireProvider mGeofireProvider;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    //BANDERA PARA SABER SI DEBO SOLICITAR LOS PERMISOS DE UBICACIÓN
    private final static int LOCATION_REQUEST_CODE = 1;

    private final static int SETTINGS_REQUEST_CODE = 2;

    //PROPIEDAD PARA PONER EL ICONO EN EL MAPA
    private Marker mMarker;

    //ALMACENARE LA LOCALIZACIÓN EN UNA VARIABLE LOCAL PARA UTILIZAR LA POSICIÓN EN TIEMPO REAL
    private LatLng mCurrentLatLng;

    private TokenProvider mTokenProvider;

    private TextView mtextViewClientBooking;
    private TextView mtextViewEmailClientBooking;
    private TextView mtextViewOriginClientBooking;
    private TextView mtextViewDestinationClientBooking;
    private ImageView mImageViewBooking;

    private String mExtraClientId;

    private ClientProvider mClientProvider;

    private ClientBookingProvider mClientBookingProvider;

    private NotificationProvider mNotificationProvider;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolyLineOptions;

    //PARA VALIDAR QUE SEA LA PRIMERA VEZ QUE ENTRA AL CALLBACK
    private boolean mIsFirstTime = true;

    private Button mButtonStartBooking;
    private Button mButtonFinishBooking;

    private boolean mIsCloseToClient = false;

    //POSICIÓN DEL CLIENTE
    private LocationCallback mLocationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //RECORREMOS LA PROPIEDAD LOCATION
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //PARA VALIDAR QUE NO SE CREEN MUCHOS MARCADORES A LA VEZ
                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    //CON ESTA LINEA AGREGAMOS EL MARCADOR CON LAS POSICIONES DE LAT Y LONG DE NUESTRA VARIABLE LOCATION
                    //UN TITULO Y AGREGAMOS EL ICONO QUE SELECCIONAMOS
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            ).title("Mi posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi))
                    );
                    //OBTENER LA UBICACIÓN DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(17f)
                                    .build()
                    ));
                    updateLocation();

                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        getClientBooking();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver_booking);

        //Instancio mi objeto provider
        mAuthProvider = new AuthProvider();

        //Nuevo nodo
        mGeofireProvider = new GeofireProvider("drivers_working");

        mTokenProvider = new TokenProvider();

        mClientProvider = new ClientProvider();

        mNotificationProvider = new NotificationProvider();

        mClientBookingProvider = new ClientBookingProvider();

        mGoogleApiProvider = new GoogleApiProvider(MapDriverBookingActivity.this);

        //Recibe el contexto. Con esto se puede iniciar o detener la ubicación del usuario a comveniencia
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Este metodo recibe un callback y nuestra actividad la implementa
        mMapFragment.getMapAsync(this);

        mtextViewClientBooking = findViewById(R.id.textViewClientBooking);
        mtextViewEmailClientBooking = findViewById(R.id.textViewEmailClientBooking);
        mtextViewOriginClientBooking = findViewById(R.id.textViewOriginClientBooking);
        mtextViewDestinationClientBooking = findViewById(R.id.textViewDestinationClientBooking);
        mImageViewBooking = findViewById(R.id.imageViewClientBooking);

        mButtonStartBooking = findViewById(R.id.btnStartBooking);
        mButtonFinishBooking = findViewById(R.id.btnFinishBooking);

        //mButtonStartBooking.setEnabled(false);

        mExtraClientId = getIntent().getStringExtra("idClient");

        getClient();

        mButtonStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsCloseToClient) {
                    startBooking();
                }
                else {
                    Toast.makeText(MapDriverBookingActivity.this, "Debes estar más cerca a la posición de recogida", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mButtonFinishBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishBooking();
            }
        });
    }

    private void startBooking() {
        mClientBookingProvider.updateStatus(mExtraClientId, "start");
        mButtonStartBooking.setVisibility(View.GONE);
        mButtonFinishBooking.setVisibility(View.VISIBLE);
        //Limpia el mapa
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.pinverde)));
        drawRoute(mDestinationLatLng);
        sendNotification("Iniciado");
    }

    //SABER DSITANCIA DESDE EL LUGAR DE RECOGIDA DEL CLIENTE HASTA EL PUNTO FINAL
    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng) {
        double distance = 0;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);
        distance = clientLocation.distanceTo(driverLocation);
        return distance;
        }

    private void finishBooking() {
        mClientBookingProvider.updateStatus(mExtraClientId, "finish");
        mClientBookingProvider.updateIdHistoryBooking(mExtraClientId);
        sendNotification("Finalizado");
        if (mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallBack);
        }
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        Intent intent = new Intent(MapDriverBookingActivity.this, CalificationClientActivity.class);
        intent.putExtra("idClient", mExtraClientId);
        startActivity(intent);
        finish();
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String destination = snapshot.child("destination").getValue().toString();
                    String origin = snapshot.child("origin").getValue().toString();
                    double destinationLat = Double.parseDouble(snapshot.child("destinationLat").getValue().toString());
                    double destinationLng = Double.parseDouble(snapshot.child("destinationLng").getValue().toString());
                    double originLng = Double.parseDouble(snapshot.child("originLng").getValue().toString());
                    double originLat = Double.parseDouble(snapshot.child("originLat").getValue().toString());

                    mOriginLatLng = new LatLng(originLat, originLng);
                    mDestinationLatLng = new LatLng(destinationLat, destinationLng);
                    mtextViewOriginClientBooking.setText("Recoger en: "+origin);
                    mtextViewDestinationClientBooking.setText("Destino: "+destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Recoger aquí").icon(BitmapDescriptorFactory.fromResource(R.drawable.pinrojo)));
                    drawRoute(mOriginLatLng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void drawRoute(LatLng latLng){
        //TRAZAR LA RUTA DESDE LA POSCIÓN DEL CLIENTE AL CONDUCTOR
        mGoogleApiProvider.getDirections(mCurrentLatLng, latLng).enqueue(new Callback<String>() {
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

    private void getClient() {
        //OBTIENE LA INFORMACIÓN UNA UNICA VEZ
        mClientProvider.getClient(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String email = snapshot.child("email").getValue().toString();
                    String name = snapshot.child("nombre").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")) {
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(MapDriverBookingActivity.this).load(image).into(mImageViewBooking);
                    }
                    mtextViewClientBooking.setText(name);
                    mtextViewEmailClientBooking.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateLocation() {
        //PODRIA OBTENER EL ID ASÍ PERO SERIA UNA MALA PRÁCTICA, ASÍ QUE IRÁ AL PROVIDER DE AUTH
        //mGeofireProvider.saveLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //Validar si la sesión está activa y la posición existe
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
            if (!mIsCloseToClient) {
                if (mOriginLatLng != null && mCurrentLatLng != null) {
                    double distance = getDistanceBetween(mOriginLatLng, mCurrentLatLng); //RETORNA EN METROS
                    if(distance <=200) {
                        //mButtonStartBooking.setEnabled(true);
                        mIsCloseToClient = true;
                        Toast.makeText(this, "Estas cerca a la posición del cliente", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        //PARA QUE APAREZCA EL PUNTO EN EL MAPA
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(false);
        mLocationRequest = new LocationRequest();
        //Intervalo para actualizar la ubicación el usuario en el mapa 1000-3000
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        //Prioridad del GPS para trabajar. HIGH para que sea precisa
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
        //EMPIECE A ESCUCHAR APENAS SE ABRA EL MAPA, YA NO HAY UN BOTON CONECTAR AQUÍ, MAS FACHERO
        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            //SABER SI EL USUARIO OTORGO LOS PERMISOS
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        //SE DEBE OTORGAR EL PERMISO EN EL MANIFEST
                        //Se inicia el escuchador de nuestra posición actual
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());
                    }
                    else {
                        showAlertDialogNOGPS();
                    }
                }
                else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    //SABER SI EL GPS ESTÁ ACTIVO. MERA VALIDACIÓN
    private boolean gpsActived() {
        boolean isActive = false;
        //Obtiene en la variable el servicio
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Si tiene el gps activado retorna true
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Iguala el requestCode a nuestra variable global y pregunta si el gps está activado
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            //Se inicia el escuchador de nuestra posición actual
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            //Está linea necesitaba permisos con alt + enter los agregué como se ve arriba
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());
        }
        else {
            //Si no activó el gps mostramos el alertDialog
            showAlertDialogNOGPS();
        }
    }

    //METODO PARA MOSTRAR UN DIALOG QUE DIGA QUE NECESITA ENCENDER SU GPS
    private void showAlertDialogNOGPS() {
        //Creamos el dialog con este contexto
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //Le creamos el mensaje y el boton más su acción
        builder.setMessage("Por favor se necesita que active su GPS para continuar").setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Este método es como el startActivity pero espera hasta que el usuario realice una acción, en este caso el GPS
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),SETTINGS_REQUEST_CODE);
            }
        }).create().show();
    }

    //MÉTODO PARA DESCONECTAR Y CONECTAR AL CONDUCTOR
    private void disconnect() {
        if (mFusedLocation != null) {
            mFusedLocation.removeLocationUpdates(mLocationCallBack);
            if (mAuthProvider.existSession()) {
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        }
        else {
            Toast.makeText(this, "No es posible desconectarse", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        //SI LA VERSION SDK DE ANDRODID ES MAYOR A LA DE MASSMELLOW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(gpsActived()) {
                    //AL EJECUTARSE ESTE METODO ENTRA AL CALLBACK PARA OBTENER LA UBICACION EN TIEMPO REAL
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());
                }
                else {
                    showAlertDialogNOGPS();
                }
            }
            //SI LOS PERMISOS NO ESTÁN CONCEDIDOS
            else{
                checkLocationPermissions();
            }
        } else {
            if(gpsActived()) {
                //Escuchador para obtener la ubicación actual
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());
            }
            else {
                showAlertDialogNOGPS();
            }
        }
    }

    //SI EL USUARIO NO ACEPTA LOS PERMISOS
    private void checkLocationPermissions() {
        //SI LOS PERMISOS SON DISTINTO A CONCEDIDOS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicación requiere de los permisos de ubicación para poder utilizarse.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //HABILITA LOS PERMISOS DE UBICACIÓN DEL CELULAR
                                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
                //PARA MOSTRAR EL ALERT DIALOG SON LOS METODOS DE ARRIBITA
            }
            else {
                ActivityCompat.requestPermissions(MapDriverBookingActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void sendNotification(final String status) {
        //NECESITAMOS EL TO QUE VENDRIA SIENDO EL TOKEN DEL DATABASE DEL CONDUCTOR MAS CERCANO
        //ESE METODO OBTIENE EL VALOR UNA VEZ
        mTokenProvider.getToken(mExtraClientId).addListenerForSingleValueEvent(new ValueEventListener() {
            //El DataSnapshot contiene la información de todo el nodo
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //VALIDAMOS EN CASO NO OBTENGA UN TOKEN
                if (snapshot.exists()) {
                    //OBTENDREMOS EL TOKEN
                    String token = snapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "ESTADO DE TU VIAJE");
                    if (status.equals("Iniciado")) {
                        map.put("body",
                                "Ponte cómodo y disfruta del viaje. Tu viaje ha: " + status);
                    }
                    else {
                        map.put("body",
                                "Muchas gracias por acompañarnos. Tu viaje ha: " + status);
                    }
                    FCMBody fcmBody = new FCMBody(token, "high", "4500s", map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                //SI NO SE LLEGÓ A ENVIAR LA NOTIFICACIÓN
                                if (response.body().getSuccess() != 1){
                                    Toast.makeText(MapDriverBookingActivity.this, "La notificación no ha podido ser enviada", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(MapDriverBookingActivity.this, "La notificación no ha podido ser enviada", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("ERROR", "Error "+t.getMessage());

                        }
                    });
                }
                else {
                    Toast.makeText(MapDriverBookingActivity.this, "La notificación no ha podido ser enviada porque el conductor no posee un token de sesión", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}