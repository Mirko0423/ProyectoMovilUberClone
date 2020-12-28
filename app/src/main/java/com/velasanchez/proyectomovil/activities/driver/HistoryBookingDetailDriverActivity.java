package com.velasanchez.proyectomovil.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.HistoryBookingDetailClientActivity;
import com.velasanchez.proyectomovil.models.HistoryBooking;
import com.velasanchez.proyectomovil.providers.ClientProvider;
import com.velasanchez.proyectomovil.providers.DriverProvider;
import com.velasanchez.proyectomovil.providers.HistoryBookingProvider;

import de.hdodenhof.circleimageview.CircleImageView;

public class HistoryBookingDetailDriverActivity extends AppCompatActivity {

    private TextView mTextViewName;
    private TextView mTextViewOrigin;
    private TextView mTextViewDestination;
    private TextView mTextViewYourCalification;
    private RatingBar mRatinBarCalification;
    private CircleImageView mCircleImage;
    private CircleImageView mCircleImageBack;

    private String mExtraId;

    private HistoryBookingProvider mHistoryBookingProvider;
    private ClientProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_booking_detail_driver);

        mTextViewName = findViewById(R.id.textViewNameBookingDetail);
        mTextViewOrigin = findViewById(R.id.textViewOriginHistoryBookingDetail);
        mTextViewDestination = findViewById(R.id.textViewDestinationHistoryBookingDetail);
        mTextViewYourCalification = findViewById(R.id.textViewCalificationHistoryBookingDetail);
        mRatinBarCalification = findViewById(R.id.ratingBarHistoryBookingDetail);
        mCircleImage = findViewById(R.id.circleImageHistoryBookingDetail);
        mCircleImageBack =findViewById(R.id.circleImageHistoryBack);

        mExtraId = getIntent().getStringExtra("idHistoryBooking");

        mHistoryBookingProvider = new HistoryBookingProvider();
        mClientProvider = new ClientProvider();

        getHistoryBooking();

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getHistoryBooking() {
        mHistoryBookingProvider.getHistoryBooking(mExtraId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HistoryBooking historyBooking = snapshot.getValue(HistoryBooking.class);
                    mTextViewOrigin.setText(historyBooking.getOrigin());
                    mTextViewDestination.setText(historyBooking.getDestination());
                    mTextViewYourCalification.setText("Calificación del Cliente: " + historyBooking.getCalificationClient());
                    if (snapshot.hasChild("calificationClient")) {
                        mRatinBarCalification.setRating((float) historyBooking.getCalificationDriver());
                    }

                    mClientProvider.getClient(historyBooking.getIdClient()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.child("nombre").getValue().toString();
                                mTextViewName.setText(name.toUpperCase());
                                if (snapshot.hasChild("image")) {
                                    String image = snapshot.child("image").getValue().toString();
                                    Picasso.with(HistoryBookingDetailDriverActivity.this).load(image).into(mCircleImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}