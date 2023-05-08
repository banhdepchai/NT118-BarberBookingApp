package com.example.androidbarberapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    Button btn_logout;
    TextView tv;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        btn_logout = (Button)findViewById(R.id.btn_logout);
        tv = (TextView)findViewById(R.id.tv_hello);
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        tv.setText("Hello, " + currentUser.getEmail());

        btn_logout.setOnClickListener(view -> {
            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

    }
}