package com.velasanchez.proyectomovil.activities.client;

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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.MainActivity;
import com.velasanchez.proyectomovil.activities.driver.MapDriverActivity;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.GeofireProvider;
import com.velasanchez.proyectomovil.providers.TokenProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {

    AuthProvider mAuthProvider;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private GeofireProvider mGeoFireProvider;

    //PROPIEDAD PARA PONER EL ICONO EN EL MAPA
    private Marker mMarker;

    //BANDERA PARA SABER SI DEBO SOLICITAR LOS PERMISOS DE UBICACIÓN
    private final static int LOCATION_REQUEST_CODE = 1;

    private final static int SETTINGS_REQUEST_CODE = 2;

    //LISTA DE CONDUCTORES
    private List<Marker> mDriversMarkers = new ArrayList<>();

    //ALMACENARE LA LOCALIZACIÓN EN UNA VARIABLE LOCAL PARA UTILIZAR LA POSICIÓN EN TIEMPO REAL
    private LatLng mCurrentLatLng;

    //Validar para que el callback no se actualice cada que se mueve la persona
    private boolean mIsFirstTime = true;

    //PARA ESCUCHAR LA POSICIÓN QUE ASIGNEMOS
    private GoogleMap.OnCameraIdleListener mCameraListener;

    private AutocompleteSupportFragment mAutocomplete;
    private AutocompleteSupportFragment mAutocompleteDestination;

    private PlacesClient mPlaces;

    //ALMACENARA EL NOMBRE DEL ORIGEN SELECCIONADO
    private String mOrigin;
    //ALMACENARA LA LAT Y LONG DEL ORIGEN SELECCIONADO
    private LatLng mOriginLatLng;
    //ALMACENARA EL NOMBRE DEL DESTINO SELECCIONADO
    private String mDestination;
    //ALMACENARA LA LAT Y LONG DEL DESTINO SELECCIONADO
    private LatLng mDestinationLatLng;

    private Button mButtonRequestDriver;

    private TokenProvider mTokenProvider;

    private LocationCallback mLocationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            //RECORREMOS LA PROPIEDAD LOCATION
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    /*
                    //PARA VALIDAR QUE NO SE CREEN MUCHOS MARCADORES A LA VEZ
                    if (mMarker != null) {
                        mMarker.remove();
                    }
                    */

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    /*
                    //CON ESTA LINEA AGREGAMOS EL MARCADOR CON LAS POSICIONES DE LAT Y LONG DE NUESTRA VARIABLE LOCATION
                    //UN TITULO Y AGREGAMOS EL ICONO QUE SELECCIONAMOS
                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            ).title("Mi posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
                    );
                     */
                    //OBTENER LA UBICACIÓN DEL USUARIO EN TIEMPO REAL
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(17f)
                                    .build()
                    ));
                    if (mIsFirstTime) {
                        mIsFirstTime = false;
                        getActiveDrivers();
                        //LLAMAMOS AQUÍ EL METODO PQ EL USUARIO YA ESTABLECIO SU LOCALIZACIÓN Y ASÍ LIMITAMOS SU BUSQUEDA
                        limitSearch();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);

        MyToolbar.show(this, "Cliente", false);

        //Instancio mi objeto provider
        mAuthProvider = new AuthProvider();

        mGeoFireProvider = new GeofireProvider("active_drivers");

        mTokenProvider = new TokenProvider();

        //Recibe el contexto. Con esto se puede iniciar o detener la ubicación del usuario a comveniencia
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Este metodo recibe un callback y nuestra actividad la implementa
        mMapFragment.getMapAsync(this);

        mButtonRequestDriver = findViewById(R.id.btnRequestDriver);
        mButtonRequestDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDriver();
            }
        });

        if (!Places.isInitialized()) {
            //Utilizamos el contexto y el api key como parametros
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        //INICIALIZAMOS LAS VARIABLES DE GOOGLE PLACES
        mPlaces = Places.createClient(this);
        instanceAutocompleteOrigin();
        instanceAutocompleteDestination();
        onCameraMove();

        generateToken();
    }

    private void requestDriver() {
        //VALIDAR QUE EXISTA ORIGEN Y DESTINO
        if (mOriginLatLng != null && mDestinationLatLng != null) {
            Intent intent = new Intent(MapClientActivity.this, DetailRequestActivity.class);
            //PARA MANDARNOS ESOS VALORES
            intent.putExtra("origin_lat", mOriginLatLng.latitude);
            intent.putExtra("origin_lng", mOriginLatLng.longitude);
            intent.putExtra("destination_lat", mDestinationLatLng.latitude);
            intent.putExtra("destination_lng", mDestinationLatLng.longitude);
            intent.putExtra("origin", mOrigin);
            intent.putExtra("destination", mDestination);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Debe seleccionar un lugar de recogida y el lugar de destino", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCameraMove() {
        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            //METODO PARA CUANDO CAMBIEMOS LA POSICIÓN DEL USUARIO EN CAMARA
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapClientActivity.this);
                    //CUANDO EL USUARIO SE MUEVA
                    mOriginLatLng = mMap.getCameraPosition().target;
                    List<Address> addressList = geocoder.getFromLocation(mOriginLatLng.latitude, mOriginLatLng.longitude, 1);
                    //PRIMERO OBTENER LA CIUDAD
                    String city = addressList.get(0).getLocality();
                    //LUEGO EL PAIS
                    String country = addressList.get(0).getCountryName();
                    //AL FINAL DIRECCIÓN
                    String address = addressList.get(0).getAddressLine(0);

                    mOrigin = address + ", " + city;
                    mAutocomplete.setText(address + ", " + city);
                } catch (Exception e){
                    Log.d("Error: ", "Mensaje error: " + e.getMessage());
                }
            }
        };
    }

    private void limitSearch() {
        //Distancia en km
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000, 180);
        mAutocomplete.setCountry("PER");
        mAutocomplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        mAutocompleteDestination.setCountry("PER");
        mAutocompleteDestination.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }

    private void instanceAutocompleteOrigin() {
        //CON EL PLACEAUTOCOMPLETE DEL ACTIVITY
        mAutocomplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigin);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        //TEXTASO DEL COMPLETEPLACE
        mAutocomplete.setHint("Lugar de recogida");
        //Un escuchador para cuando presionen sobre el
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            //DEVUELVE EL LUGAR SELECCIONADO POR EL USUARIO EN EL OBJETO PLACE Y ESE DATO LO GUARDARÉ PARA DESPUES
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //Obtiene el nombre
                mOrigin = place.getName();
                //Obtiene coordenadas
                mOriginLatLng = place.getLatLng();
                Log.d("PLACE", "NAME: " + mOrigin);
                Log.d("COORDENADAS", "Latitud: " + mOriginLatLng.latitude);
                Log.d("COORDENADAS", "Longitud: " + mOriginLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void instanceAutocompleteDestination() {
        //CON EL PLACEAUTOCOMPLETE DEL ACTIVITY
        mAutocompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestination);
        mAutocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        //TEXTASO
        mAutocompleteDestination.setHint("Lugar de Destino");
        //Un escuchador para cuando presionen sobre el
        mAutocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            //DEVUELVE EL LUGAR SELECCIONADO POR EL USUARIO EN EL OBJETO PLACE Y ESE DATO LO GUARDARÉ PARA DESPUES
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //Obtiene el nombre
                mDestination = place.getName();
                //Obtiene coordenadas
                mDestinationLatLng = place.getLatLng();
                Log.d("PLACE", "NAME: " + mDestination);
                Log.d("COORDENADAS", "Latitud: " + mDestinationLatLng.latitude);
                Log.d("COORDENADAS", "Longitud: " + mDestinationLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void getActiveDrivers() {
                        mGeoFireProvider.getActiveDrivers(mCurrentLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
                            //AQUI AÑADIREMOS LOS MARCADORES DE LOS CONDUCTORES QUE IRAN CONECTANDOSE
                            @Override
                            public void onKeyEntered(String key, GeoLocation location) {
                                //CREAMOS UNA PROPIEDAD DE TIPO MARKER Y RECORREREMOS LA LISTA
                                for (Marker marker : mDriversMarkers) {
                                    //Ver si se le asigno un identificador a ese marcador
                                    if (marker.getTag() != null) {
                                        //La key es de la bd cuando se conecta un conductor
                                        if (marker.getTag().equals(key)) {
                                            //Así no se vuelva a añadir un conductor porque ya estaria conectado
                                            return;
                                        }
                                    }
                }
                //OBTENEMOS LA POSICIÓN DEL CONDUCTOR
                LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Conductor disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi)));
                marker.setTag(key);
                mDriversMarkers.add(marker);
            }

            //AQUI VAMOS A ELIMINAR LOS MARCADORES DE LOS CONDUCTORES QUE SE DESCONECTEN DE LA APP
            @Override
            public void onKeyExited(String key) {
                for (Marker marker : mDriversMarkers) {
                    //Ver si se le asigno un identificador a ese marcador
                    if (marker.getTag() != null) {
                        //La key es de la bd cuando se conecta un conductor
                        if (marker.getTag().equals(key)) {
                            //REMOVEMOS EL MARCADOR
                            marker.remove();
                            mDriversMarkers.remove(marker);
                            //Así no se vuelva a añadir un conductor porque ya estaria conectado
                            return;
                        }
                    }
                }
            }

            //ACTUALIZA EN TIEMPO REAL LA POSICIÓN DEL CONDUCTOR MIENTRAS SE VA MOVIENDO
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marker : mDriversMarkers) {
                    //Ver si se le asigno un identificador a ese marcador
                    if (marker.getTag() != null) {
                        //La key es de la bd cuando se conecta un conductor
                        if (marker.getTag().equals(key)) {
                            //Establece el marcador en una nueva posición
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(mCameraListener);

        mLocationRequest = new LocationRequest();
        //Intervalo para actualizar la ubicación el usuario en el mapa 1000-3000
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        //Prioridad del GPS para trabajar. HIGH para que sea precisa
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        starLocation();
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
                        mMap.setMyLocationEnabled(true);
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
            mMap.setMyLocationEnabled(true);
        }
        else if(requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
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

    private void starLocation() {
        //SI LA VERSION SDK DE ANDRODID ES MAYOR A LA DE MASSMELLOW
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if(gpsActived()) {
                    //AL EJECUTARSE ESTE METODO ENTRA AL CALLBACK PARA OBTENER LA UBICACION EN TIEMPO REAL
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
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
                mMap.setMyLocationEnabled(true);
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
                                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
                //PARA MOSTRAR EL ALERT DIALOG SON LOS METODOS DE ARRIBITA
            }
            else {
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Parametros el menú que creamos y el parametro del onCreateOptionsMenu
        getMenuInflater().inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_update) {
            Intent intent = new Intent(MapClientActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_history) {
            Intent intent = new Intent(MapClientActivity.this, HistoryBookingClientActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        mAuthProvider.logout();
        Intent intent = new Intent(MapClientActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void generateToken() {
        mTokenProvider.create(mAuthProvider.getId());
    }
}