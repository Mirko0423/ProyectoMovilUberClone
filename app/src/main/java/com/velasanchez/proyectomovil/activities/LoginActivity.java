package com.velasanchez.proyectomovil.activities;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.velasanchez.proyectomovil.R;
import com.velasanchez.proyectomovil.activities.client.MapClientActivity;
import com.velasanchez.proyectomovil.activities.client.RegisterActivity;
import com.velasanchez.proyectomovil.activities.driver.MapDriverActivity;
import com.velasanchez.proyectomovil.includes.MyToolbar;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText mtxtEmail;
    TextInputEditText mtxtPassword;
    Button mbtnLogin;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    private CircleImageView mCircleImageView;

    AlertDialog mDialog;

    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //MyToolbar.show(this, "Inicio de Sesión", true);

        //Inicializamos el preferences
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        mCircleImageView = findViewById(R.id.circleImageBack);

        mtxtEmail = findViewById(R.id.txtEmail);
        mtxtPassword = findViewById(R.id.txtPassword);
        mbtnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        /*Intancia del alert dialog que jalamos del repositorio de GitHub*/
        mDialog = new SpotsDialog.Builder().setContext(LoginActivity.this).setMessage("Espere un momento").build();

        mbtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void login() {
        String email = mtxtEmail.getText().toString();
        String password = mtxtPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){
            if (password.length() >= 6){
                /*Utilizamos el Dialog del GitHub*/
                mDialog.show();
                /*Que hacer cuando se ejecuta la función de validación de cuenta*/
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        /*Si es correcta la validación*/
                        if(task.isSuccessful()){
                            String user = mPref.getString("user", "");
                            if (user.equals("client")) {
                                //Para redireccionar al mapa
                                Intent intent = new Intent(LoginActivity.this, MapClientActivity.class);
                                //Asegurarnos que no pueda regresar al registro
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            else {
                                //Para redireccionar al mapa
                                Intent intent = new Intent(LoginActivity.this, MapDriverActivity.class);
                                //Asegurarnos que no pueda regresar al registro
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                            //Toast.makeText(LoginActivity.this, "El inicio de sesión se realizó satisfactoriamente", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "El email o la contraseña son incorrectos", Toast.LENGTH_SHORT).show();
                        }
                        /*Ocultamos el dialog al terminar el login*/
                        mDialog.dismiss();
                    }
                });
            }
            else{
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "El email y el password son obligatorios", Toast.LENGTH_SHORT).show();
        }
    }
}