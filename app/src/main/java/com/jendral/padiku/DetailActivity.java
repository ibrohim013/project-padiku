package com.jendral.padiku;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class DetailActivity extends AppCompatActivity {
    TextView txt_nama_hama,
            txt_nama_gejala,
            txt_nama_solusi;
    ImageView imageView;
    ProgressDialog progressDialog;
    Context context;
    String title, hasil_gejala, hasil_solusi, gambar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        txt_nama_hama = findViewById(R.id.txt_nama_hama);
        txt_nama_gejala = findViewById(R.id.txt_nama_gejala);
        txt_nama_solusi = findViewById(R.id.txt_nama_solusi);
        imageView = findViewById(R.id.imageView);
        Objects.requireNonNull(getSupportActionBar()).hide();

        context = this;
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            title = bundle.getString("title");
            gambar = bundle.getString("gambar");
            hasil_gejala = bundle.getString("hasil_gejala");
            hasil_solusi = bundle.getString("hasil_solusi");


            txt_nama_hama.setText(title);
            txt_nama_gejala.setText(hasil_gejala);
            txt_nama_solusi.setText(hasil_solusi);
            Glide.with(context).load(gambar).placeholder(R.drawable.image_default).into(imageView);
        }


    }
}