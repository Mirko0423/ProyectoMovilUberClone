package com.velasanchez.proyectomovil.providers;

import android.content.Context;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.velasanchez.proyectomovil.util.CompressorBitmapImage;

import java.io.File;

public class ImageProvider {

    private StorageReference mStorage;

    public ImageProvider(String ref) {
        mStorage = FirebaseStorage.getInstance().getReference().child(ref);
    }

    public UploadTask saveImage(Context context, File image, String idUser) {
        //CLASE DE INTERNET PARA COMPRIMIR LAS IMAGENES, RECIBE EL CONTEXTO, EL PATH DE LA IMAGEN, Y EL TAMAÑO EN PIXELES
        byte[] imageByte = CompressorBitmapImage.getImage(context, image.getPath(), 500, 500);
        //EL PRIMER CHILD AQUÍ GENERA EL NOMBRE DE LA CARPETA DENTRO DEL STORAGE
        final StorageReference storage = mStorage.child(idUser + ".jpg");
        mStorage = storage;
        UploadTask uploadTask = storage.putBytes(imageByte);
        return  uploadTask;
    }

    public StorageReference getStorage() {
        return  mStorage;
    }

}
