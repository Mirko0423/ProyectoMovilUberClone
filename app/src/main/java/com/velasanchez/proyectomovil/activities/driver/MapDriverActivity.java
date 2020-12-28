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
import android.graphics.Camera;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.MainActivity;
import com.velasanchez.proyectomovil.activities.client.HistoryBookingClientActivity;
import com.velasanchez.proyectomovil.activities.client.MapClientActivity;
import com.velasanchez.proyectomovil.activities.client.UpdateProfileActivity;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.GeofireProvider;
import com.velasanchez.proyectomovil.providers.TokenProvider;

public class MapDriverActivity extends AppCompatActivity implements OnMapReadyCallback {

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

    private Button mButtonConnect;
    private boolean mIsConnect = false;

    private TokenProvider mTokenProvider;

    private ValueEventListener mListener;

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
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);

        //Instancio mi objeto provider
        mAuthProvider = new AuthProvider();

        mGeofireProvider = new GeofireProvider("active_drivers");

        mTokenProvider = new TokenProvider();

        MyToolbar.show(this, "Conductor", false);

        //Recibe el contexto. Con esto se puede iniciar o detener la ubicación del usuario a comveniencia
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Este metodo recibe un callback y nuestra actividad la implementa
        mMapFragment.getMapAsync(this);

        mButtonConnect = findViewById(R.id.btnConectar);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsConnect){
                    disconnect();
                }
                else {
                    startLocation();
                }
            }
        });

        generateToken();
        isDriverWorking();
    }

    //Lo mismo para detener el escuchador
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mGeofireProvider.isDriverWorking(mAuthProvider.getId()).removeEventListener(mListener);
        }
    }

    private void isDriverWorking() {
        //METODO PARA ESCUCHAR EN TIEMPO REAL
        mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    //Metodo para dejar de escuchar la localización en tiempo real y eliminaba el nodo de active drivers
                    disconnect();
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
            mButtonConnect.setText("Conectarse");
            mIsConnect = false;
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
                    mButtonConnect.setText("Desconectarse");
                    mIsConnect = true;
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
                                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
                        //PARA MOSTRAR EL ALERT DIALOG SON LOS METODOS DE ARRIBITA
            }
            else {
                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Parametros el menú que creamos y el parametro del onCreateOptionsMenu
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MapDriverActivity.this, UpdateProfileDriverActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MapDriverActivity.this, HistoryBookingDriverActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        disconnect();
        mAuthProvider.logout();
        Intent intent = new Intent(MapDriverActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }
}