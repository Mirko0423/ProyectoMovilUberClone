package com.velasanchez.proyectomovil.activities.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.models.Client;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientProvider;
import com.velasanchez.proyectomovil.providers.ImageProvider;
import com.velasanchez.proyectomovil.util.CompressorBitmapImage;
import com.velasanchez.proyectomovil.util.FileUtil;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {

    private ImageView mImageViewProfile;
    private Button btnUpdate;
    private TextView mTextViewName;
    private CircleImageView mCircleImageView;

    private ClientProvider mClientProvider;
    private AuthProvider mAuthProvider;
    private ImageProvider mImageProvider;

    private File mImageFile;
    private String mImage;

    private final int GALLERY_REQUEST = 1;
    private ProgressDialog mProgressDialog;
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        //MyToolbar.show(this, "Actualizar perfil", true);

        mImageViewProfile = findViewById(R.id.imageViewProfile);
        mTextViewName = findViewById(R.id.txtUsuario);
        btnUpdate = findViewById(R.id.btnUpdateProfile);
        mCircleImageView = findViewById(R.id.circleImageBack);

        mClientProvider = new ClientProvider();
        mAuthProvider = new AuthProvider();
        mImageProvider = new ImageProvider("client_images");
        mProgressDialog = new ProgressDialog(this);

        getClientInfo();

        mImageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ABRIR LA GALERIA DE IMAGENES PERO DEBEMOS DARLE PERMISO
                openGallery();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        //Devuelve el resultado, si el usuario seleccionó una imagen o no
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    //SOBREESCRIBIMOS PARA SABER SI SELECCIONÓ O NO LA IMGAEN DE LA GALERIA
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLERY_REQUEST && resultCode == RESULT_OK) {
            try {
                //ESA CLASE FILEUTIL LA BAJAMOS DE INTERNET ES LA QUE CONVIERTE LO QUE SUBIMOS A LO ACEPTADOS POR FIREBASE STORAGE
                mImageFile = FileUtil.from(this, data.getData());
                mImageViewProfile.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
            } catch (Exception e) {
                Log.d("ERROR", "Mensaje: "+e.getMessage());
            }
        }
    }

    private void getClientInfo() {
        mClientProvider.getClient(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = snapshot.child("nombre").getValue().toString();
                    String image = "";
                    if (snapshot.hasChild("image")) {
                        image = snapshot.child("image").getValue().toString();
                        Picasso.with(UpdateProfileActivity.this).load(image).into(mImageViewProfile);
                    }
                    mTextViewName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateProfile() {
        mName = mTextViewName.getText().toString();
        if (!mName.equals("")  && mImageFile != null) {
            //En lo que sube la imagen
            mProgressDialog.setMessage("Espere un momento...");
            //PARA QUE NO CANCELE EL PROGRESS
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

            saveImage();
        }
        else {
            Toast.makeText(this, "Ingresa la imagen y el nombre", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        mImageProvider.saveImage(UpdateProfileActivity.this, mImageFile, mAuthProvider.getId()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                //CONFIRMA SI SE SUBIO O NO LA IMAGEN AL STORAGE
                if (task.isSuccessful()) {
                    //PARA OBTENER EL LINK DEL STORAGE Y PODER MOSTRAR LA IMAGEN
                    mImageProvider.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String image = uri.toString();
                            //ACUALIZAMOS EL FIREBASE DATABASE DEL CLIENTE CON LA URL DE SU IMAGEN
                            Client client = new Client();
                            client.setImage(image);
                            client.setNombre(mName);
                            client.setId(mAuthProvider.getId());
                            mClientProvider.update(client).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(UpdateProfileActivity.this, "Su información se actualizó correctamente", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    });
                }
                else {
                    Toast.makeText(UpdateProfileActivity.this, "Hubo un error al subir la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}