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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.models.Info;
import com.velasanchez.proyectomovil.providers.GoogleApiProvider;
import com.velasanchez.proyectomovil.providers.InfoProvider;
import com.velasanchez.proyectomovil.util.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private String mExtraOrigin;
    private String mExtraDestination;

    private CircleImageView mCircleImageView;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolyLineOptions;

    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewTime;
    private TextView mTextViewPrice;

    private Button mButtonRequest;

    private InfoProvider mInfoProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);

        //MyToolbar.show(DetailRequestActivity.this, "Tus datos", true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //Este metodo recibe un callback y nuestra actividad la implementa
        mMapFragment.getMapAsync(this);

        mCircleImageView = findViewById(R.id.circleImageBack);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");


        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);
        mInfoProvider = new InfoProvider();

        mTextViewOrigin =findViewById(R.id.textViewOrigin);
        mTextViewDestination =findViewById(R.id.textViewDestination);
        mTextViewTime=findViewById(R.id.textViewTime);
        mTextViewPrice =findViewById(R.id.textViewPrice);

        mButtonRequest = findViewById(R.id.btnRequestNow);

        mTextViewOrigin.setText(mExtraOrigin);
        mTextViewDestination.setText(mExtraDestination);

        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestDriver();
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void goToRequestDriver() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra("origin_lat", mOriginLatLng.latitude);
        intent.putExtra("origin_lng", mOriginLatLng.longitude);
        intent.putExtra("origin", mExtraOrigin);
        intent.putExtra("destination", mExtraDestination);
        intent.putExtra("destination_lat", mDestinationLatLng.latitude);
        intent.putExtra("destination_lng", mDestinationLatLng.longitude);
        startActivity(intent);
        //Para cerrar esta actividad al pasar al RequestDriverActivity
        finish();
    }

    private void drawRoute(){
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng).enqueue(new Callback<String>() {
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

                    mTextViewTime.setText(durationText + " " + distanceText);

                    //DIVIDIMOS PQ REGRESA EL NUMERO Y EL KM, DIVIDIMOS EN EL ESPACIO
                    String[] distanceAndKm = distanceText.split(" ");
                    double distanceValue = Double.parseDouble(distanceAndKm[0]);

                    String[] durationAndMins = durationText.split(" ");
                    double durationValue = Double.parseDouble(durationAndMins[0]);

                    calculatePrice(distanceValue, durationValue);

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

    private void calculatePrice(final double distanceValue, final double durationValue) {
        //ESE METODO RETORNA LA INFORMACION
        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Info info = snapshot.getValue(Info.class);
                    double totalDistance = distanceValue * info.getKm();
                    double totalDuration = durationValue * info.getMin();
                    double total = totalDistance + totalDuration;
                    double minTotal = total - 0.5;
                    double maxTotal = total +0.5;
                    mTextViewPrice.setText("S/. " + minTotal + " - " + maxTotal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.pinrojo)));
        mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.pinverde)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mOriginLatLng)
                        .zoom(17f)
                        .build()
        ));
        drawRoute();
    }
}