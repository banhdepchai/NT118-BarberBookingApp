package com.example.androidbarberapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    TextInputEditText edt_email, edt_password;
    Button btn_login;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView tv_register_now;



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), Home.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        edt_email = (TextInputEditText)findViewById(R.id.edt_email);
        edt_password = (TextInputEditText)findViewById(R.id.edt_password);
        btn_login = (Button)findViewById(R.id.btn_login);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        tv_register_now = (TextView)findViewById(R.id.tv_register_now);

        tv_register_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
                finish();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email = String.valueOf(edt_email.getText());
                String password = String.valueOf(edt_password.getText());

                if(TextUtils.isEmpty(email))
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Please enter email address and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password))
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Please enter email address and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this, Home.class);
                                startActivity(intent);
                                finish();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user.
                                Toast.makeText(Login.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            }
        });
    }
}