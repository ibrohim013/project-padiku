package com.jendral.padiku.sheet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jendral.padiku.R;
import com.jendral.padiku.model.dataGejala;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.Objects;

public class dialogGejala extends DialogFragment {
    Context context;
    String pilih;
    dataGejala data_gejala;

    public dialogGejala(Context context, String pilih) {
        this.context = context;
        this.pilih = pilih;
    }

    public dialogGejala(Context context, String pilih, dataGejala data_gejala) {
        this.context = context;
        this.pilih = pilih;
        this.data_gejala = data_gejala;
    }

    ProgressDialog progressDialog;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    StorageReference storage = FirebaseStorage.getInstance().getReference();
    ImageView iv_gejala;
    Button btn_image,
            btn_simpan;
    EditText txt_nama_hama,
            txt_gejala,
            txt_nama_solusi;
    Uri fileUri;
    Bitmap bitmap;
    String nama_hama;
    String gejala;
    String nama_solusi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.form_gejala, container, false);
        iv_gejala = v.findViewById(R.id.iv_gejala);
        btn_image = v.findViewById(R.id.btn_image);
        btn_simpan = v.findViewById(R.id.btn_simpan);
        txt_nama_hama = v.findViewById(R.id.txt_nama_hama);
        txt_gejala = v.findViewById(R.id.txt_gejala);
        txt_nama_solusi = v.findViewById(R.id.txt_nama_solusi);

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        if (pilih.equals("ubah")) {
            Glide.with(context).load(data_gejala.getGambar()).placeholder(R.drawable.image_default).into(iv_gejala);
            txt_nama_hama.setText(data_gejala.getJudul());
            txt_gejala.setText(data_gejala.getGejala());
            txt_nama_solusi.setText(data_gejala.getSolusi());
        }

        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nama_hama = txt_nama_hama.getText().toString();
                gejala = txt_gejala.getText().toString();
                nama_solusi = txt_nama_solusi.getText().toString();
                if (nama_hama.isEmpty()) {
                    txt_nama_hama.setError("Data tidak boleh kosong");
                    txt_nama_hama.requestFocus();
                } else if (gejala.isEmpty()) {
                    txt_gejala.setError("Data tidak boleh kosong");
                    txt_gejala.requestFocus();
                } else if (nama_solusi.isEmpty()) {
                    txt_nama_solusi.setError("Data tidak boleh kosong");
                    txt_nama_solusi.requestFocus();
                } else if (bitmap == null && pilih.equals("tambah")) {
                    Toast.makeText(context, "Masukan gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
                } else {
                    progressDialog.setMessage("Loading");
                    progressDialog.show();
                    if (pilih.equals("tambah")) {
                        final String key = database.push().getKey();
                        storage.child("gejala_solusi").child(key).putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storage.child("gejala_solusi").child(key).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        saveData(key, uri.toString(), "disimpan");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        dismiss();
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                dismiss();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @SuppressLint("DefaultLocale")
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                float proses = 100.f * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                                progressDialog.setMessage(String.format("Upload %.2f", proses) + "%");
                            }
                        });
                    } else {
                        if (bitmap == null) {
                            saveData(data_gejala.getKey(), data_gejala.getGambar(), "diubah");
                        } else {
                            storage.child("gejala_solusi").child(data_gejala.getKey()).putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    storage.child("gejala_solusi").child(data_gejala.getKey()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            saveData(data_gejala.getKey(), uri.toString(), "diubah");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            dismiss();
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    dismiss();
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @SuppressLint("DefaultLocale")
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                                    float proses = 100.f * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                                    progressDialog.setMessage(String.format("Upload %.2f", proses) + "%");
                                }
                            });
                        }
                    }
                }
            }
        });

        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(context)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_PICK);
                                intent.setType("image/*");
                                startActivityForResult(Intent.createChooser(intent, "Cari Gambar"), 10);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });


        return v;
    }

    private void saveData(String key, String img, final String ket) {
        database.child("gejala").child(key).setValue(new dataGejala(
                nama_hama,
                gejala,
                nama_solusi,
                img
        )).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Data berhasil " + ket, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                fileUri = data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), fileUri);
                iv_gejala.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
