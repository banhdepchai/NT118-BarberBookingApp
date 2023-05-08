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

import org.w3c.dom.Text;

public class Register extends AppCompatActivity {

    TextInputEditText edt_email, edt_password;
    Button btn_register;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView tv_login_now;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        edt_email = (TextInputEditText)findViewById(R.id.edt_email);
        edt_password = (TextInputEditText)findViewById(R.id.edt_password);
        btn_register = (Button)findViewById(R.id.btn_register);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        tv_login_now = (TextView)findViewById(R.id.tv_login_now);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email = String.valueOf(edt_email.getText());
                String password = String.valueOf(edt_password.getText());

                if(TextUtils.isEmpty(email))
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Register.this, "Please enter email address and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password))
                {
                    Toast.makeText(Register.this, "Please enter email address and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Register.this, "Account created successfully",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Register.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        tv_login_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Register.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}