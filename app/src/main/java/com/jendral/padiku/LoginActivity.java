package com.jendral.padiku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jendral.padiku.model.dataLogin;

public class LoginActivity extends AppCompatActivity {
    EditText username,
            password;
    Button masuk,
            daftar;
    Switch rememberMe;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        rememberMe = findViewById(R.id.rememberMe);
        password = findViewById(R.id.password);
        masuk = findViewById(R.id.masuk);
        daftar = findViewById(R.id.daftar);

        masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = username.getText().toString();
                final String sandi = password.getText().toString();
                if (user.isEmpty()) {
                    username.setError("Data tidak boleh kosong");
                    username.requestFocus();
                } else if (sandi.isEmpty()) {
                    password.setError("Data tidak boleh kosong");
                    password.requestFocus();
                } else {
                    database.child("login").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(user).exists()) {
                                dataLogin data = dataSnapshot.child(user).getValue(dataLogin.class);
                                if (data != null) {
                                    if (data.getUsername().equals(user)) {
                                        if (data.getPassword().equals(sandi)) {
                                            if (rememberMe.isChecked()) {
                                                startToActivity(true, data);
                                            } else {
                                                startToActivity(false, data);
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Kata sandi salah", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Username salah", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Username belum terdaftar", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, DaftarActivity.class));
            }
        });
    }

    private void startToActivity(boolean active, dataLogin data) {
        preferences.setActiveData(LoginActivity.this, active);
        preferences.setKeyData(LoginActivity.this, data.getUsername());
        preferences.setNamaData(LoginActivity.this, data.getNama());
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (preferences.getActiveData(LoginActivity.this)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}