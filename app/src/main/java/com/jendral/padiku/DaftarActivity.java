package com.jendral.padiku;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jendral.padiku.model.dataDaftar;
import com.jendral.padiku.model.dataLogin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class DaftarActivity extends AppCompatActivity {
    EditText nama,
            email,
            kota,
            telepon,
            sandi,
            konfirmasi_sandi;
    Button masuk;
    private int PERMISSION_GALERI = 101;
    private int ACCESS_GALERI = 201;
    Bitmap bitmap;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    ProgressDialog dialog;
    CircleImageView cricleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        cricleView = findViewById(R.id.cricleView);
        nama = findViewById(R.id.nama);
        email = findViewById(R.id.email);
        kota = findViewById(R.id.kota);
        telepon = findViewById(R.id.telepon);
        sandi = findViewById(R.id.sandi);
        konfirmasi_sandi = findViewById(R.id.konfimasi_sandi);
        masuk = findViewById(R.id.masuk);

        masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String _nama = nama.getText().toString();
                String _email = email.getText().toString();
                String _kota = kota.getText().toString();
                String _telepon = telepon.getText().toString();
                String _sandi = sandi.getText().toString();
                String _konfirmasi_sandi = konfirmasi_sandi.getText().toString();

                if(_nama.isEmpty()){
                    nama.setError("Data tidak boleh kosong");
                    nama.requestFocus();
                }else if(_email.isEmpty()){
                    email.setError("Data tidak boleh kosong");
                    email.requestFocus();
                }else if (_kota.isEmpty()){
                    kota.setError("Data tidak boleh kosong");
                    kota.requestFocus();
                }else if (_telepon.isEmpty()){
                    telepon.setError("Data tidak boleh kosong");
                    telepon.requestFocus();
                }else if (_sandi.isEmpty()){
                    sandi.setError("Data tidak boleh kosong");
                    sandi.requestFocus();
                }else if (_konfirmasi_sandi.isEmpty()){
                    konfirmasi_sandi.setError("Data tidak boleh kosong");
                    konfirmasi_sandi.requestFocus();
                }else {
                    if (bitmap == null) {
                        saveDatatoDatabase("");
                    } else {
                        saveUploadImage(bitmap);
                    }
                }
            }
        });

        cricleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(DaftarActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DaftarActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_GALERI);
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Pilih gambar"), ACCESS_GALERI);
                }
            }
        });

    }

    private void saveDatatoDatabase(String image) {
        final String _username = String.valueOf(System.currentTimeMillis());
        final String _nama = nama.getText().toString();
        final String _password = sandi.getText().toString();

        String _email = email.getText().toString();
        String _kota = kota.getText().toString();
        String _telepon = telepon.getText().toString();

        database.child("data-user").child(_nama)
                .setValue(new dataDaftar(_username,_nama,_email,
                        _kota,
                        _telepon,image)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                database.child("login")
                        .child(_nama)
                        .setValue(new dataLogin(_username, _nama, _password))
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                                clearEditText();
                                startActivity(new Intent(DaftarActivity.this, LoginActivity.class));
                                Toast.makeText(DaftarActivity.this, "Data berhasil terdaftar", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        clearEditText();
                        Toast.makeText(DaftarActivity.this, "Data gagal terdaftar", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(DaftarActivity.this, LoginActivity.class));
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                clearEditText();
                Toast.makeText(DaftarActivity.this, "Data gagal terdaftar", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(DaftarActivity.this, LoginActivitydafta.class));
            }
        });
    }

    private void clearEditText() {
        nama.setText(null);
                email.setText(null);
        kota.setText(null);
                telepon.setText(null);
        sandi.setText(null);
                konfirmasi_sandi.setText(null);
    }

    private void saveUploadImage(Bitmap bitmapGambar) {
        String _username = String.valueOf(System.currentTimeMillis());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapGambar.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        byte[] dataUpload = byteArrayOutputStream.toByteArray();

        final StorageReference uploadStorage = storage.child(_username);

        uploadStorage.putBytes(dataUpload)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploadStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                saveDatatoDatabase(uri.toString());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(DaftarActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(DaftarActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                float progress = 100.0f * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                dialog.setMessage(String.format("Sign Up Success %.2f", progress) + " %");
                dialog.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_GALERI) {
            ActivityCompat.requestPermissions(DaftarActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_GALERI);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACCESS_GALERI && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri uriGaleri = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriGaleri);
                cricleView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
