package com.jendral.padiku.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jendral.padiku.R;
import com.jendral.padiku.adapter.CommunicationRecyclerAdapter;
import com.jendral.padiku.model.dataCommunication;
import com.jendral.padiku.preferences;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DashboardFragment extends Fragment {
    EditText sendMessage;
    FloatingActionButton fab_send;
    RecyclerView recyclerView;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    ArrayList<dataCommunication> listCommunication = new ArrayList<>();
    ArrayList<String> listData = new ArrayList<>();
    CommunicationRecyclerAdapter communicationRecyclerAdapter;
    Context context;
    Toolbar toolbar;
    ImageView add_data;
    int ACCESS_DATA = 40;
    int PERMISSION_DATA = 20;
    ProgressDialog dialog;
    Locale locale = new Locale("in", "ID");
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMMM/yyyy", locale);
    String id;
    String name;
    Uri capturedImageURI;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
//        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        context = v.getContext();

        id = preferences.getKeyData(context);
        name = preferences.getNamaData(context);

        sendMessage = v.findViewById(R.id.sendMessage);
        add_data = v.findViewById(R.id.add_data);
        toolbar = v.findViewById(R.id.toolbar);
        fab_send = v.findViewById(R.id.fab_send);
        recyclerView = v.findViewById(R.id.recyclerView);

        dialog = new ProgressDialog(context);

        toolbar.inflateMenu(R.menu.menu_action);

        fab_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMessage();
            }
        });

        add_data.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_DATA);
                } else {

                    capturedImageURI = Uri.fromFile(createImegeFile());


                    Intent captureIntent = new Intent();

                    captureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageURI);

                    Intent galeryIntent = new Intent();
                    galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galeryIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    galeryIntent.setType("image/jpeg");

                    Intent choserIntent = Intent.createChooser(galeryIntent, "Select Picture");
                    choserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                    startActivityForResult(choserIntent, ACCESS_DATA);
                }
            }
        });

        sendDataMessageShow();
        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_DATA) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSION_DATA);
        }
    }

    Bitmap bitmap;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACCESS_DATA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                capturedImageURI = data.getData();
            }

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), capturedImageURI);
                sendImageMessage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendImageMessage(Bitmap bitmap) {
        final String nameImage = "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

        final StorageReference storage = FirebaseStorage.getInstance().getReference().child("media").child("image").child(nameImage);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        byte[] bytesData = byteArrayOutputStream.toByteArray();
        dialog.setCancelable(false);
        final String send = sendMessage.getText().toString();

        storage.putBytes(bytesData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        database.child("pesan").push().setValue(new dataCommunication(
                                id,
                                name,
                                uri.toString(),
                                simpleDateFormat.format(System.currentTimeMillis()),
                                System.currentTimeMillis(),
                                "image",
                                nameImage
                        )).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                float progress = 100.0f * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                dialog.setMessage(String.format("Upload %.2f ", progress) + "%");
                dialog.show();
            }
        });

    }

    private void inputMessage() {
        String send = sendMessage.getText().toString();
        if (send.isEmpty()) {
            sendMessage.setError("Masukan pesan");
            sendMessage.requestFocus();
        } else {
            database.child("pesan")
                    .push()
                    .setValue(new dataCommunication(
                            id,
                            name,
                            send,
                            simpleDateFormat.format(System.currentTimeMillis()),
                            System.currentTimeMillis(),
                            "text"))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Pesan berhasil dikirim", Toast.LENGTH_SHORT).show();
                            sendMessage.setText(null);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Pesan gagal dikirim", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendDataMessageShow() {
        database.child("pesan").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listCommunication.clear();
                listData.clear();
                listData.add("");
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    dataCommunication communication = item.getValue(dataCommunication.class);
                    if (communication != null) {
                        communication.setPush(item.getKey());
                    }
                    listData.add(communication != null ? communication.getTanggal() : null);
                    listCommunication.add(communication);
                }
                communicationRecyclerAdapter = new CommunicationRecyclerAdapter(context, listCommunication, listData, toolbar);
                recyclerView.setAdapter(communicationRecyclerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public File createImegeFile() {
        File imageStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");

        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }

        return new File(
                imageStorageDir + File.separator + "IMG_"
                        + String.valueOf(System.currentTimeMillis())
                        + ".jpg");

    }
}
