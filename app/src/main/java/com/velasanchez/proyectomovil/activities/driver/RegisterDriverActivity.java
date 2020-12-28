package com.velasanchez.proyectomovil.activities.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.MapClientActivity;
import com.velasanchez.proyectomovil.activities.client.RegisterActivity;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.models.Client;
import com.velasanchez.proyectomovil.models.Driver;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientProvider;
import com.velasanchez.proyectomovil.providers.DriverProvider;

import dmax.dialog.SpotsDialog;

public class RegisterDriverActivity extends AppCompatActivity {

    /* Ya no los usamos pq creamos providers
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    */
    AuthProvider mAuthProvider;
    DriverProvider mDriverProvider;

    AlertDialog mDialog;

    Button mButtonRegister;
    TextInputEditText mtxtEmail;
    TextInputEditText mtxtPassword;
    TextInputEditText mtxtUsuario;
    TextInputEditText mtxtPlacaVehiculo;
    TextInputEditText mtxtMarcaVehiculo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        //TOOL BAR
        MyToolbar.show(this, "Registrar Conductor", true);

        mButtonRegister = findViewById(R.id.btnRegister);
        mtxtEmail = findViewById(R.id.txtEmail);
        mtxtUsuario = findViewById(R.id.txtUsuario);
        mtxtPassword = findViewById(R.id.txtPassword);
        mtxtMarcaVehiculo = findViewById(R.id.txtMarcaVehiculo);
        mtxtPlacaVehiculo = findViewById(R.id.txtPlacaVehiculo);

        /* Ya no los usamos pq creamos providers
        mAuth = FirebaseAuth.getInstance();
        //Intancias y haces referencia al nodo principal de la base
        mDatabase = FirebaseDatabase.getInstance().getReference();
        */
        mAuthProvider = new AuthProvider();
        mDriverProvider = new DriverProvider();

        /*Intancia del alert dialog que jalamos del repositorio de GitHub*/
        mDialog = new SpotsDialog.Builder().setContext(RegisterDriverActivity.this).setMessage("Espere un momento").build();

        mButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickRegister();
            }
        });
    }

    void clickRegister() {
        final String usuario = mtxtUsuario.getText().toString();
        final String email = mtxtEmail.getText().toString();
        final String password = mtxtPassword.getText().toString();
        final String marcaVehiculo = mtxtMarcaVehiculo.getText().toString();
        final String placaVehiculo = mtxtPlacaVehiculo.getText().toString();

        if (!usuario.isEmpty() && !email.isEmpty() && !password.isEmpty() && !marcaVehiculo.isEmpty() && !placaVehiculo.isEmpty()){
            if (password.length() >=6 ){
                mDialog.show();
                //Metodo para crear un nuevo usuario
                register(usuario, email, password, marcaVehiculo, placaVehiculo);
            }
            else {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    void register(final String usuario, final String email, String password, final String marcaVehiculo, final String placaVehiculo) {
        //Con esto pasamos toda la logica del metodo createUserWithEmailAndPassword en una clase aparte y solo dejamo el OnClick en la actividad.
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if (task.isSuccessful()){
                    //Obtener el id del firebase authentication de la cuenta que se está logeando
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Driver driver = new Driver(id, usuario, email, marcaVehiculo, placaVehiculo);
                    create(driver);
                }
                else {
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void create(Driver driver) {
        mDriverProvider.create(driver).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Solo era por mientras Gg
                    //Toast.makeText(RegisterDriverActivity.this, "El registro se realizó exitosamente", Toast.LENGTH_SHORT).show();

                    //Para redireccionar al mapa
                    Intent intent = new Intent(RegisterDriverActivity.this, MapDriverActivity.class);
                    //Asegurarnos que no pueda regresar al registro
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(RegisterDriverActivity.this, "No se pudo registrar al cliente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}