package com.jendral.padiku.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.jendral.padiku.MainActivity;
import com.jendral.padiku.R;
import com.jendral.padiku.ViewAnimation;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static Context mContext;
    private ImageView imageView;
    private TextView textView,gejala,title_gejala,title_solusi,
    solusi;
    private ProgressDialog dialog;
    private static final int ACCESS_FILE = 10;
    private static final int ACCESS_CAMERA = 40;
    private static final int PERMISSION_FILE = 20;
    private static final int PERMISSION_CAMERA = 30;
    FirebaseAutoMLRemoteModel remoteModel =
            new FirebaseAutoMLRemoteModel.Builder("Penyakit_20206614454").build();
    FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
            .requireWifi()
            .build();

    FirebaseAutoMLLocalModel localModel = new FirebaseAutoMLLocalModel.Builder()
            .setAssetFilePath("model/manifest.json")
            .build();

    FloatingActionButton fab_add,
            fab_photo,
            fab_image;

    boolean isRotate = false;
    RelativeLayout lineProsess;
    Thread thread;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mContext = v.getContext();
        fab_add = v.findViewById(R.id.fab_add);
        fab_photo = v.findViewById(R.id.fab_photo);
        fab_image = v.findViewById(R.id.fab_image);
        title_gejala = v.findViewById(R.id.title_gejala);
        title_solusi = v.findViewById(R.id.title_solusi);
        lineProsess = v.findViewById(R.id.lineProsess);

        imageView = v.findViewById(R.id.image);
        textView = v.findViewById(R.id.textView);
        gejala = v.findViewById(R.id.gejala);
        solusi = v.findViewById(R.id.solusi);
        dialog = new ProgressDialog(v.getContext());


        ViewAnimation.init(fab_photo);
        ViewAnimation.init(fab_image);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showFab();
            }
        });
        fab_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(HomeFragment.mContext, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions((Activity) HomeFragment.mContext,new String[]{Manifest.permission.CAMERA},PERMISSION_CAMERA);
                }else {
                    Intent intent = new Intent();
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,ACCESS_CAMERA);
                }

            }
        });



        fab_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(HomeFragment.mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions((Activity) HomeFragment.mContext,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_FILE);
                }else{
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent,"Pilih gambar"),ACCESS_FILE);


                }


            }
        });


        return v;
    }

    private void showFab() {
        isRotate = ViewAnimation.rotateFab(fab_add,!isRotate);
        if (isRotate){
            showAnimationIn();
        }else{
            showAnimatinOut();
        }
    }


    private void showAnimationIn(){
        ViewAnimation.showIn(fab_photo);
        ViewAnimation.showIn(fab_image);

    }

    private void showAnimatinOut(){
        ViewAnimation.showOut(fab_photo);
        ViewAnimation.showOut(fab_image);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACCESS_FILE && resultCode == Activity.RESULT_OK && data != null && data.getData() !=null){
            try {
                Uri uri = data.getData();
                FirebaseModelManager.getInstance().download(remoteModel, conditions);
                textView.setText("");
                Bitmap bitmapStorage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uri);
                imageView.setImageBitmap(bitmapStorage);
                setLabelerFromLocalModel(bitmapStorage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(requestCode == ACCESS_CAMERA && resultCode == Activity.RESULT_OK){
            Bundle build = data.getExtras();
            Bitmap bitmapCamera = (Bitmap) build.get("data");
            imageView.setImageBitmap(bitmapCamera);
            setLabelerFromLocalModel(bitmapCamera);
        }
    }

    private void setLabelerFromLocalModel(Bitmap uri) {
//        showProgressDialog();
        lineProsess.setVisibility(View.VISIBLE);

        try {
            FirebaseVisionOnDeviceAutoMLImageLabelerOptions options =
                    new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(localModel)
                            .setConfidenceThreshold(0.0f)
                            .build();

            final FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
            final FirebaseVisionImage image = FirebaseVisionImage.fromBitmap( uri);

            thread = new Thread(){
                 public void run(){
                     try {
                         sleep(7000);
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }finally{
                         processImageLabeler(labeler,image);
                     }
                 }
            };
            thread.start();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

    }
    String eachLabel;
    ArrayList<String> dataHasil = new ArrayList<>();
    private void processImageLabeler(FirebaseVisionImageLabeler labeler, FirebaseVisionImage image) {
        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                dialog.dismiss();
                dataHasil.clear();
                lineProsess.setVisibility(View.GONE);
                for(FirebaseVisionImageLabel label : labels){
                    eachLabel = label.getText().toUpperCase();
//                    float confidence =  label.getConfidence();
//                    textView.append(eachLabel+ " : "+ (""+confidence * 100).subSequence(0,4)+"%"+"\n");
                    dataHasil.add(eachLabel);
                }
                String hasil = dataHasil.get(0).replace("_"," ");
                textView.setText(hasil);
                title_gejala.setText("Gejala");
                title_solusi.setText("Cara Pengendalian");
                fab_add.setVisibility(View.GONE);

                if(hasil.equals("PENGGEREK BATANG PADI")){

                    gejala.setText( "Terdapatnya penggerek di lapangan dapat dilihat dari adanya ngengat di pertanaman dan larva di dalam batang. Mekanisme kerusakan yaitu larva merusak sistem pembuluh tanaman di dalam batang. Penggerek batang padi menyerang tanaman padi pada semua fase pertumbuhan tanaman, menimbulkan gejala sundep pada fase pertumbuhan vegetatif dan beluk (malai hampa) pada fase pertumbuhan generatif.");
                    solusi.setText("Cara Pengendalian\n" +
                            "1. Daerah serangan Epidermis\n" +
                            "\t\tPengaturan Polan tanam\n" +
                            "• Dilakukan penanaman serentak, sehingga tersedianya sumber makanan bagi penggerek batang padi dapat dibatasi. Pergiliran tanaman dengan tanaman bukan padi sehingga dapat memutus siklus hidup hama.\n" +
                            "• Pergiliran tanaman pada daerah endemis hendaknya diikuti dengan pergiliran varietas padi yang toleran.\n" +
                            "• Pengelompokan persemaian dimaksudkan untuk memudahkan upaya pengumpulan telur penggerek secara massal. \n" +
                            "• Pengaturan waktu tanam yaitu berdasarkan penerbangan ngengat atau populasi larva ditanggul padi, yaitu :\n" +
                            "• 15 hari sesudah puncak penerbangan ngengat generasi pertama \n" +
                            "• Dan atau 15 hari sesudah puncak penerbangan ngengat generasi berikutnya\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "\tPengendalian cara fisik dan mekanik\n" +
                            "• Cara fisik yaitu pada saat panen dilakukan penyabitan tanaman serendah mungkin sampai permukaan tanah, diikuti penggenangan air setinggi - 10 cm agar jerami atau pangkal jerami cepat membusuk sehingga larva atau pupa mati. \n" +
                            "• Cara mekanik dilakukan dengan mengumpulkan kelompok telur penggerek batang padi di persemaian dan di pertanaman. Telur-telur yang terkumpul dipelihara (antara lain dalam bumbung bambu) dan apabila keluar parasitoid, dilepaskan kembali ke pertanaman.\n" +
                            "\tPengendalian hayati\n" +
                            "• Pemanfaatan musuh alami dilakukan dengan jalar pengumpulan kelompok telur dan pelepasan kembal parasitoid. \n" +
                            "• Dilakukan pengembangbiakan parasitoid Trichogramma sp. pada telur Corcyra sp. \n" +
                            "• Konservasi musuh alami \n" +
                            "• Pelepasan parasitoid larva, parasitoid pupa dan predator telur seperti tersebut di atas.\n" +
                            "\tPenggunaan insektisida \n" +
                            "Aplikasi insektisida dilakukan bila keadaan serangan melebih Ambang Pengendalian atau jika populasi ngengat/imago meningkat pada saat tanaman fase generatif. Gunakan insektisida yang berbahan aktif antara lain: dimehipo, karbofuran, fipronil, bisultap, karbosulfan, imidakloprid. abamektin, bensultap, kartap hidroklorida, monosultap, amitraz, monosultap, klorantraniliprol, spinetoram, flubendiamida, profurit aminium atau spinosad.\n" +
                            "2. Daerah serangan sporadis\n" +
                            "Cara pengendalian selain menggunakan insektisida yang dapat diterapkan sesuai dengan keadaan setempat. \n" +
                            "Penyemprotan dengan insektisida berdasarkan hasil pengamatan, yaitu apabila ditemukan rata-rata > 1 kelompok telur/3m? atau intensitas serangan penggerek batang padi (sundep) rata-rata > 10%-15% tergantung varietas tanaman padi, dan beluk rata-rata > 10% selambat-lambatnya tiga minggu sebelum panen.\n");
                }else  if(hasil.equals("WERENG HIJAU")){
                    gejala.setText("Hama");
                    solusi.setText("Obat Padi");
                }

                showFab();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeFragment.mContext,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog(){
        dialog.setMessage("Loading...");
        dialog.setCancelable(false);
        dialog.show();

    }
}
