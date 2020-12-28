package com.velasanchez.proyectomovil.activities.client;

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
import com.velasanchez.proyectomovil.activities.driver.MapDriverActivity;
import com.velasanchez.proyectomovil.activities.driver.RegisterDriverActivity;
import com.velasanchez.proyectomovil.includes.MyToolbar;
import com.velasanchez.proyectomovil.models.Client;
import com.velasanchez.proyectomovil.providers.AuthProvider;
import com.velasanchez.proyectomovil.providers.ClientProvider;

import dmax.dialog.SpotsDialog;

public class RegisterActivity extends AppCompatActivity {

    SharedPreferences mPref;

    /* Ya no los usamos pq creamos providers
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    */
    AuthProvider mAuthProvider;
    ClientProvider mClientProvider;

    Toolbar mToolbar;

    AlertDialog mDialog;

    Button mButtonRegister;
    TextInputEditText mtxtEmail;
    TextInputEditText mtxtPassword;
    TextInputEditText mtxtUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //TOOL BAR
        MyToolbar.show(this, "Registrar Cliente", true);

        mButtonRegister = findViewById(R.id.btnRegister);
        mtxtEmail = findViewById(R.id.txtEmail);
        mtxtUsuario = findViewById(R.id.txtUsuario);
        mtxtPassword = findViewById(R.id.txtPassword);

        /* Ya no los usamos pq creamos providers
        mAuth = FirebaseAuth.getInstance();
        //Intancias y haces referencia al nodo principal de la base
        mDatabase = FirebaseDatabase.getInstance().getReference();
        */
        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();

        /*Intancia del alert dialog que jalamos del repositorio de GitHub*/
        mDialog = new SpotsDialog.Builder().setContext(RegisterActivity.this).setMessage("Espere un momento").build();

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

        if (!usuario.isEmpty() && !email.isEmpty() && !password.isEmpty()){
            if (password.length() >=6 ){
                mDialog.show();
                //Metodo para crear un nuevo usuario
                register(usuario, email, password);
            }
            else {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(this, "Ingrese todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    void register(final String usuario, final String email, String password) {
        //Con esto pasamos toda la logica del metodo createUserWithEmailAndPassword en una clase aparte y solo dejamo el OnClick en la actividad.
        mAuthProvider.register(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mDialog.hide();
                if (task.isSuccessful()){
                    //Obtener el id del firebase authentication de la cuenta que se está logeando
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Client client = new Client(id, usuario, email);
                    create(client);
                }
                else {
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void create(Client client) {
        mClientProvider.create(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //Solo era por mientrar Gg
                    //Toast.makeText(RegisterActivity.this, "El registro se realizó exitosamente", Toast.LENGTH_SHORT).show();

                    //Para redireccionar al mapa
                    Intent intent = new Intent(RegisterActivity.this, MapClientActivity.class);
                    //Asegurarnos que no pueda regresar al registro
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar al cliente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
    void saveUser(String id, String usuario, String email) {
        //Recibimos el preferences que en este caso era el user, y le damos un valor por defecto en caso no reciva nada
        String selectedUser = mPref.getString("user", "");
        //Instanciamos un nuevo usuario de la clase que creamos
        User user = new User();
        user.setEmail(email);
        user.setNombre(usuario);

        if (selectedUser.equals("driver")) {
            //Metodo push para crear un identificador unico en el nodo
            //Pero nosotros usaremos el identificador de firebaseauthentication
            mDatabase.child("Users").child("Drivers").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else if (selectedUser.equals("client")){
            mDatabase.child("Users").child("Clients").child(id).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
     */
}