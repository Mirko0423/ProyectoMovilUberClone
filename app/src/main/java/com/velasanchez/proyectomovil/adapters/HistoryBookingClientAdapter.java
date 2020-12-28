package com.velasanchez.proyectomovil.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.HistoryBookingDetailClientActivity;
import com.velasanchez.proyectomovil.models.HistoryBooking;
import com.velasanchez.proyectomovil.providers.DriverProvider;

public class HistoryBookingClientAdapter extends FirebaseRecyclerAdapter<HistoryBooking, HistoryBookingClientAdapter.ViewHolder> {

    private DriverProvider mDriverProvider;
    private Context mContext;

    public HistoryBookingClientAdapter(FirebaseRecyclerOptions<HistoryBooking> options, Context context){
        super(options);
        mDriverProvider = new DriverProvider();
        mContext = context;
    }

    //DONDE ESTABLECEREMOS LOS VALORES DE LAS VISTAS PARA NUESTRAS TARJETAS
    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull HistoryBooking historyBooking) {
        //Nos devuelve el id de cada uno de nuestros historiales de viaje segun su posicion
        final String id = getRef(position).getKey();
        //EL OBJETO HOLDER NOS AYUDA A TENER ACCESO A CADA UNO DE LOS ELEMENTOS DE NUESTRO VIEW HOLDER
        holder.textViewOrigin.setText(historyBooking.getOrigin());
        holder.textViewDestination.setText(historyBooking.getDestination());
        holder.textViewCalification.setText(String.valueOf(historyBooking.getCalificationClient()));
        mDriverProvider.getDriver(historyBooking.getIdDriver()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("nombre").getValue().toString();

                    holder.textViewName.setText(name);
                    if (snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.with(mContext).load(image).into(holder.imageViewHistoryBooking);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //EL VIEW ES NUESTRA TARJETA
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, HistoryBookingDetailClientActivity.class);
                intent.putExtra("idHistoryBooking",id);
                mContext.startActivity(intent);
            }
        });
    }

    //INSTANCIAREMOS EL LAYOUT QUE UTILIZAMOS, NUESTRO CARD
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_history_booking, parent, false);
        return new ViewHolder(view);
    }

    //AQUI VAMOS A INSTANCIAR TODAS NUESTRAS VISTAS
    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;
        private TextView textViewOrigin;
        private TextView textViewDestination;
        private TextView textViewCalification;
        private ImageView imageViewHistoryBooking;
        private View mView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            textViewName = view.findViewById(R.id.textViewName);
            textViewOrigin= view.findViewById(R.id.textViewOrigin);
            textViewDestination = view.findViewById(R.id.textViewDestination);
            textViewCalification = view.findViewById(R.id.textViewCalification);
            imageViewHistoryBooking = view.findViewById(R.id.imageViewHistoryBooking);
        }
    }

}
