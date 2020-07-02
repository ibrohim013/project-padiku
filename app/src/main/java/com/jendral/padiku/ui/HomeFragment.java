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
                }else  if(hasil.equals("WERENG BATANG COKLAT")){
                    gejala.setText("Wereng batang coklat (WBC) sebelumnya termasuk hama sekunder dan menjadi hama penting akibat penyemprotan pestisida yang tidak tepat pada awal pertumbuhan tanaman, sehingga membunuh musuh alami. Pertanaman yang dipupuk nitrogen tinggi dengan jarak tanam rapat merupakan kondisi yang sangat disukai WBC. Stadia tanaman yang rentan terhadap serangan WBC adalah dari pembibitan sampai fase matang susu. Gejala kerusakan yang ditimbulkannya adalah tanaman menguning dan cepat sekali mengering. Umumnya gejala terlihat mengumpul pada satu lokasi - melingkar disebut hopperburn. Mekanisme kerusakan adalah WBC menghisap cairan tanaman pada sistem vaskular (pembuluh tanaman).");
                    solusi.setText("    1. Pengaturan pola tanam\n" +
                            "Pengaturan pola tanam yang diterapkan adalah tanam serentak, pergiliran tanaman, dan pergiliran varietas berdasarkan tingkat ketahanan dan tingkat biotipe WBC.\n" +
                            "    2. Penggunaan varietas tahan\n" +
                            "Varietas tahan dapat digabungkan dengan cara pengendalian biologi misalnya pemanfaatan musuh alami. Agar penggunaan varietas tahan dapat bertahan lama dan efektif perlu diintegrasikan dengan komponen pengendalian yang lain, seperti pengaturan pola tanam, pergiliran varietas, sistem pengamatan yang intensif.\n" +
                            "    3. Pengendalian hayati\n" +
                            "Beberapa cendawan patogen serangga yang dapat dimanfaatkan untuk mengendalikan WBC adalah Beauveria bassiana, Metarhizium anisopliae, M. flavoviridae, dan Hirsutella citriformis. Konservasi musuh alami dengan cara menghindari penyemprotan pestisida tanpa dasar yang jelas.\n" +
                            "    4. Eradikasi\n" +
                            "Eradikasi dilakukan apabila ditemukan serangan kerdil rumput dan kerdil hampa dengan pencabutan dan pemusnahan.\n" +
                            "    5. Penggunaan insektisida\n" +
                            "Pengendalian dengan insektisida dilaksanakan apabila setelah dilakukan pengelolaan agroekosistem masih dijumpai WBC > 10 ekor/rumpun pada tanaman berumur > 40 hst atau > 40 ekor/rumpun pada tanaman berumur > 40 hst. insektisida yang digunakan bersifat selektif, efektif dan terdaftar dan diizinkan.\n" +
                            "Di daerah serangan WBC yang merupakan daerah serangan virus (kerdil rumput dan atau kerdil hampa) dilakukan penggunaan insektisida butiran pada saat satu hari sebelum pengolahan tanah terakhir secara seed bed treatment. Penyemprotan tambahan dilakukan apabila ditemukan WBC di persemaian dan pertanaman, dengan ketentuan seperti di atas. Aplikasi insektisida dilakukan bila keadaan serangan melebihi ambang pengendalian dengan menggunakan insektisida yang berbahan aktif antara lain: imidakloprid, dimehipo, BPMC, karbofuran, buprofezin, MIPC, fipronil, monosultap, karbosulfan, abamektin, bisultap, bensultap, etofenproks, kartap hidroklorida, etiprol, spinosad, tiametoksam, amitraz, propoksur, metolkarb, spinetoram, saponin, monosultap atau flubendiamida.");
                }else if (hasil.equals("HAMA PUTIH PALSU")){
                    gejala.setText("Hama putih palsu (HPP) sebenarnya jarang menjadi masalah utama di pertanaman padi. Kerusakan akibat serangan larva hama putih palsu terlihat dengan adanya warna putih pada daun di pertanaman. Larva makan jaringan hijau daun dari dalam lipatan daun meninggalkan permukaan bawah daun yang berwarna putih.\n");
                    solusi.setText("    1. Pengaturan air irigasi\n" +
                            "Cara pengendalian yang sederhana ialah dengan mengeringkan air pada persemaian dan persawahan yang terserang dalam waktu pendek (5-7 hari) untuk mencegah perpindahan larva sehingga larva mati, karena larva hanya bertahan hidup bila ada air.\n" +
                            "    2. Pengunaan insektisida\n" +
                            "Mengingat hama putih hanya menyerang tanaman muda dan banyaknya parasitoid dan predator di lapang, maka pengendalian secara kimiawi perlu dipertimbangkan secara cermat. Tanaman yang terserang dengan cepat tumbuh daun baru. Penyemprotan dengan insektisida yang efektif dan terdaftar dan diijinkan apabila ditemukan intensitas serangan pada daun bendera 15% atau rata-rata intensitas serangan pada seluruh areal sudah mencapai > 25%. Insektisida yang digunakan berbahan aktif antara lain: imidakloprid, dimehipo, BPMC, MIPC, fipronil, karbosulfan, insoksakarb, abamektin, bensultap, etofenproks, spinosad, klorantraniliprol, spinetoram, atau flubendiamida.");
                }else if (hasil.equals("WERENG HIJAU")){
                    gejala.setText("Wereng hijau merupakan hama penting karena dapat menyebarkan (vektor) virus penyebab penyakit tungro. Kepadatan populasi wereng hijau biasanya rendah, sehingga jarang menimbulkan kerusakan karena cairan tanaman dihisap oleh wereng hijau. Namun karena kemampuan pemencaran (dispersal) yang tinggi, bila ada sumber inokulum sangat efektif menyebarkan penyakit. Populasi wereng hijau hanya meningkat pada saat tanam hingga pembentukan malai. Kepadatan populasi tertinggi pada saat itu mencapai 1 ekor perrumpun. Gejala kerusakan yang ditimbulkannya adalah tanaman menjadi kerdil, anakan berkurang, daun berubah warna menjadi kuning sampai kuning oranye.");
                    solusi.setText("    1. Pergiliran tanaman dengan tanaman bukan padi\n" +
                            "    2. Penanaman varietas tahan\n" +
                            "    3. Sanitasi terhadap tanaman inang\n" +
                            "    4. Aplikasi insektisida dilakukan bila keadaan serangan melebihi ambang pengendalian dengan\n" +
                            "menggunakan insektisida yang berbahan aktif antara lain: imidakloprid, BPMC, karbofuran, tiametoksam, buprofezin, MIPC, atau etofenproks.");
                }else if (hasil.equals("ULAT GRAYAK")){
                    gejala.setText("Serangan Kerusakan terjadi karena larva makan bagian atas tanaman pada malam hari dan cuaca yang berawan. Daun yang dimakan dimulai dari tepi daun sampai hanya meninggalkan tulang daun dan batang. Larvanya sangat rakus, semua stadia tanaman padi dapat diserangnya, mulai dari pembibitan, khususnya pembibitan kering, sampai fase pengisian. M. separata dapat memotong pangkal malai dan dikenal sebagai ulat pemotong malai");
                    solusi.setText("        1. Pengendalian secara kultur teknis:\n" +
                            "    a. pengolahan tanah dengan membalikkan tanah\n" +
                            "    b. membersihkan gulma sebagai tempat hidup ulat grayak\n" +
                            "        2. Pengendalian biologi : jenis parasitoid (Telenomus sp., Cotesia sp.) dan predator jalat buah   Tachinidae, semut Formicidae serta laba-laba.\n" +
                            "        3. Pengendalian kimiawi: penyemprotan insektisida pada daerah yang terserang. Insektisida yang digunakan berbahan aktif antara lain : karbofuran atau etofenproks");
                }else if (hasil.equals("WALANG SANGIT")){
                    gejala.setText("Serangan Walang sangit merusak bulir padi pada fase pemasakan dengan cara menghisap butiran gabah yang sedang mengisi. Serangga mempertahankan diri dengan mengeluarkan bau. Fase pertumbuhan tanaman padi yang rentan terhadap serangan walang sangit adalah dari keluarnya malai sampai masak susu. Kerusakan yang ditimbulkannya menyebabkan beras berubah warna, mengapur, dan hampa.");
                    solusi.setText("            1. Pola tanam\n" +
                            "Tanam serentak dalam hamparan sawah yang cukup luas dengan perbedaan waktu tanam paling lama 2 minggu. Keserentakan tanam diartikan sebagai keserentakan memasuki fase \tmasak susu. Dengan demikian periode waktu yang cocok bagi penyerangan walang sangit berlangsung pendek.\n" +
                            "            2. Sanitasi\n" +
                            "Dilakukan sanitasi atau pembersihan tanaman inang dan tanaman-tanaman yang digunakan sebagai tempat bersembunyi di sekitar pertanaman padi.\n" +
                            "            3. Cara mekanik\n" +
                            "Dilakukan pengumpulan serangga dengan menggunakan alat perangkap, kemudian dimatikan. Sebagai alat perangkap dapat digunakan bangkai kepiting, ketam, tulang-tulang, dan sebagainya yang diletakkan di sawah. Dapat pula dilakukan dengan membakar jerami \tatau memasang lampu perangkap.\n" +
                            "        4. Penggunaan insektisida\n" +
                            "Penyemprotan dengan insektisida berbahan aktif antara lain: BPMC, metolkarb, fipronil, imidakloprid, MIPC, abamektin, propoksur, dimehipo, atau karbofuran.");
                }else if (hasil.equals("GANJUR")){
                    gejala.setText("Stadia tanaman padi yang rentan terhadap serangan ganjur adalah dari fase pembibitan sampai pembentukan malai. Larvanya memakan titik tumbuh tanaman. Ciri kerusakan yang ditimbulkannya adalah daun menggulung seperti daun bawang. Ukuran daun bawang bisa panjang, bisa juga kecil/pendek sehingga sulit dilihat. Anakan yang memiliki gejala seperti daun bawang ini tidak akan menghasilkan malai. Pada saat tanaman mencapai fase pembentukan bakal malai, laiva tidak lagi menyebabkan kerusakan.");
                    solusi.setText("            1. Waktu tanam\n" +
                            "Waktu tanam dilakukan lebih awal, yaitu 1,5 bulan sebelum puncak curah hujan tertinggi, sehingga pada saat kelembaban tinggi, tanaman sudah mencapai fase generatif. Usaha penanaman dini perlu dilakukan secara serentak.\n" +
                            "            2. Jarak tanam\n" +
                            "Jarak tanam yang terlalu rapat akan menguntungkan bagi perkembangan hama ganjur. Dianjurkan untuk menanam dengan jarak tanam 20- 25 cm dengan jumlah bibit 2-3 bibit.\n" +
                            "            3. Penyiangan\n" +
                            "Perlu dilakukan untuk menekan perkembangan hama ganjur.\n" +
                            "            4. Penggunaan insektisida yang berbahan aktif imidakloprid.");
                }else if (hasil.equals("BELALANG KEMBARA")){
                    gejala.setText("Belalang kembara fase gregaria aktif terbang pada siang hari dalam kelompok-kelompok besar. Pada senja hari, kelompok belalang hinggap pada suatu lokasi, biasanya untuk bertelur pada lahanlahan kosong berpasir, makan tanaman yang dihinggapi dan kawin, Pada pagi harinya, kelompok belalang terbang untuk berputarputar atau pindah lokasi. Pertanaman yang dihinggapi pada malam hari tersebut biasanya dimakan sampai habis. Sedangkan kelompok besar nimfa (belalang muda) biasanya berpindah tempat dengan berjalan secara berkelompok,");
                    solusi.setText("                1. Pola tanam\n" +
                            "Mengatur pola tanam dengan tanaman alternatif yang kurang/tidak disukai belalang dengan penanaman tumpang sari atau diversifikasi.\n" +
                            "                2. Pengendalian cara mekanis\n" +
                            "Melakukan gerakan massal pengendalian mekanis sesuai stadia populasi :\n" +
                            "Stadia telur\n" +
                            "    a. Untuk mengetahui adanya lokasi telur maka harus melakukan pemantauan lokasi dan waktu hinggap kelompok belalang dewasa secara intensif.\n" +
                            "    b. Pada areal tersebut atau lokasi bekas serangan yang diketahui terdapat populasi telur, dilakukan kegiatan pengumpulan kelompok telur yaitu dengan melakukan pengolahan tanah sedalam 10 cm, kelompok telur diambil dan dimusnahkan, kemudian lahannya segera ditanami kembali dengan tanaman yang tidak disukai belalang.\n" +
                            "\n" +
                            "Stadia nimfa\n" +
                            "    a. Setelah + 2 minggu sejak hinggapnya kelompok belalang kembara mulai dilakukan pemantauan terhadap kemungkinan adanya nimfa yang muncul. Pengendalian pada saat nimfa adalah kunci penting. Pengendalian nimfa dilakukan dengan cara memukul, menjaring, membakar atau perangkap lainnya. Menghalau nimfa ke suatu tempat yang sudah disiapkan di tempat terbuka untuk kemudian dimatikan. Nimfa yang sudah ada di tempat terbuka apabila memungkinkan dapat dilakukan pembakaran namun harus hati-hati agar api tidak merembet ke tempat lain.\n" +
                            "                3. Pemanfaatan agens hayati\n" +
                            "Agens hayati Metarhizium anisopliae var. acridium, Beauveria bassiana, Enthomophaga sp. \tdan Nosuma cocustal di beberapa negara terbukti dapat digunakan pada saat populasi belum meningkat. Penggunaan agens hayati digunakan sebagai pencegahan secara dini, dan  tidak efektif bila populasi sudah tidak terkendali. Penggunaan agens hayati strain lokal lebih diutamakan.\n" +
                            "                4. Penggunaan insektisida\n" +
                            "Pada keadaan populasi tinggi, dalam waktu singkat harus diupayakan penurunan populasi. Apabila cara-cara lain sudah ditempuh dan populasi masih tetap tinggi, maka alternatif lainnya yaitu penggunaan insektisida berbahan aktif antara lain: BPMC, dimehipo, beta siflutrin, deltametrin, karbaril, atau klorpirifos. Penyemprotan dengan menggunakan alat aplikasi ULV lebih baik karena lebih efisien. Pengendalian yang tepat dilakukan sejak stadia nimfa kecil karena belum merusak, lebih peka terhadap insektisida, yang dilakukan pada \tsiang hari. Apabila terpaksa karena terlambat atau tidak diketahui sebelumnya, pengendalian terhadap imago dilaksanakan pada malam hari pada saat belalang beristirahat (mulai \tbelalang hinggap pada senja hari sampai terbang waktu pagi hari).\n" +
                            "    5. Penanaman kembali\n" +
                            "Pada areal yang sudah terserang belalang dan musim tanam belum terlambat, diupayakan segera diadakan penanamar kembali dengan tanaman yang tidak disukai belalang sepert kedelai, kacang hijau, ubi kayu, ubijalar, kacang panjang tomat, kacang tanah, petsai, kubis, \tsawi atau tanaman lainnya,");
                }else if (hasil.equals("KEPINDING TANAH")){
                    gejala.setText("Kepinding tanah menyerang padi dari fase pembibitan sampai tanaman dewasa dengan cara menghisap cairan pelepah dan batang yang menyebabkan warna coklat di sekitar bagian yang dihisap. Serangan berat mengakibatkan tanaman tumbuh terhambat, warna kekuning-kuningan, kering dan akhirnya mati membusuk. Daun menjadi kering dan menggulung secara membujur.");
                    solusi.setText("        1. Cara bercocok tanam\n" +
                            "    a. Dilakukan pengolahan tanah segera setelah panen untuk mematikan telur, nimfa, dan imago yang tinggal pada pangkal tanaman padi.\n" +
                            "    b. Pengeringan lahan dapat menghambat perkembangan kepinding tanah.\n" +
                            "    c. Pemupukan pada saat terserang ringan agar tanaman mampu mengkompensasi serangan.\n" +
                            "        2. Sanitasi Sanitasi lahan dan lingkungan dari tumbuhan inang lainnya misalnya rumput-rumputan, dapat menghambat perkembangan kepinding tanah.\n" +
                            "        3. Penggunaan insektisida yang efektif, terdaftar dan diijinkan.");
                }else if (hasil.equals("LALAT BIBIT")){
                    gejala.setText("Gejala Serangan Lalat bibit menyerang tanaman padi yang baru dipindah tanam pada sawah yang selalu tergenang. Stadia hama yang merusak tanaman padi adalah larvanya. Larva lalat bibit berada di bagian tengah daun yang masih menggulung. Larva bergerak ke bagian tengah tanaman merusak jaringan bagian dalam sampai titik tumbuh daun. Gejala kerusakan lalat bibit adalah bercak-bercak kuning yang dapat dilihat di sepanjang tepi daun yang baru muncul dan daun yang terserang mengalami perubahan bentuk. Tanaman yang terserang lalat bibit anakannya menjadi berkurang. Serangan berat dapat memperlambat fase pematangan 7-10 hari. Tanaman pada dasarnya dapat mengkompensasi kerusakan asalkan tidak ada serangan hama lainnya atau tekanan lingkungan yang mempengaruhi.");
                    solusi.setText("            1. Pengaturan cara bercocok tanam dengan menunda waktu tanam yaitu beberapa minggu setelah turun hujan pertama pada awal musim hujan.\n" +
                            "            2. Sanitasi lingkungan terutama tanaman yang dapat menjadi inang lalat bibit.\n" +
                            "            3. Perlakuan benih (seed treatment) dengan insektisida yang terdaftar dan diijinkan.");
                }else if (hasil.equals("URET/LUNDI")){
                    gejala.setText("Gejala Serangan Stadia larva sangat berbahaya karena memakan akar tanaman padi dan mematikan tumbuhan tersebut. Larva memakan akar tanaman sehingga tanaman layu seperti kekurangan air, daun berwarna kuning, mengering dan akhirnya mati. Kumbang hanya makan sedikit daun-daunan dan tidak begitu merusak dibanding uretnya.");
                    solusi.setText("                1. Pengaturan pola tanam\n" +
                            "                2. Dilakukan pengolahan tanah dengan baik\n" +
                            "                3. Pengumpulan kumbang pada awal musim hujan");
                }else if (hasil.equals("ORONG ORONG")){
                    gejala.setText("Gejala Serangan Stadia tanaman yang rentan terhadap serangan hama ini adalah fase pembibitan sampai anakan. Benih yang disebar di pembibitan juga dimakan. Hama memotong tanaman pada pangkal batang dan orang sering keliru dengan gejala kerusakan yang disebabkan oleh penggerek batang (sundep). Orong-orong merusak akar muda dan bagian pangkal tanaman yang berada di bawah tanah. Tanaman padi yang terserang hama orong-orong menampakkan gejala layu, dan apabila dicabut perakarannya telah rusak dengan potongan yang khas. Peningkatan intensitas serangan akan terjadi apabila kondisi air di persawahan macak-macak.");
                    solusi.setText("                    1. Pengolahan tanah secara baik dan penggenangan sawah akan membunuh telur dan nimfa.\n" +
                            "                    2. Pengendalian mekanis pada saat pengolahan tanah terhadap anjing tanah yang berenang\n" +
                            "                    3. Penggenangan air pada lahan (pada lahan pasang surut penggenangan pada tipe luapan A dan B)\n" +
                            "                    4. Penggunaan bibit umur 35-42 hari dianjurkan hanya untuk varietas berumur panjang seperti IR42 dan Lematang\n" +
                            "                    5. Penggunaan insektisida karbofuran pada saat tanam dapat menekan intensitas serangan sampai menjadi 10%\n" +
                            "                    6. Umpan beracun yang terdiri dari satu bagian Sodium fluosillicate (atau insektisida lain) dan satu bagian gula merah yang dicampur dengan 10 bagian karir (dedak beras), kemudian \tdibuat pasta dengan mencampurkan air secukupnya\n" +
                            "                    7. Menggunakan perangkap lampu");
                }else if (hasil.equals("TIKUS")){
                    gejala.setText("Gejala Serangan Tikus merusak tanaman padi mulai dari tengah petak, kemudian meluas ke arah pinggir, dan menyisakan 1 - 2 baris padi di pinggir petakan (pada keadaan serangan berat).");
                    solusi.setText("    1. Tanam serentak\n" +
                            "Keserentakan tanaman adalah serentak memasuki fase generatif, dengan selang waktu kurang dari 10 hari yang meliputi luas + 300 ha.\n" +
                            "    2. Sanitasi dan kultur teknis\n" +
                            "    a. Lingkungan yang bersih merupakan syarat utama dalam manajemen pengendalian hama tikus agar perkembangbiakannya dapat ditekan.\n" +
                            "    b. Meminimalisasi ukuran pematang dan tanggul di sekitar persawahan. Tikus sawah lebih menyenangi tinggal pada pematang yang tingginya antara 12-30 cm dengan lebar > 60 cm.\n" +
                            "    c. Pola tanam sangat menentukan tingkat populasi hama tikus.\n" +
                            "    3. Kombinasi penggunaan anjing, pengasapan dan perangkap bambu Penelitian pengendalian hama tikus yang telah dilaksanakan ini lebih banyak \tmenggunakan anjing. Pada tahun pertama penggunaan pengasapan dan perangkap bambu hanya berperan sebagai komponen penunjang apabila penggunaan anjing sulit dilakukan. Akan tetapi pada tahun berikutnya pengasapan dan perangkap bambu lebih banyak membunuh tikus, karena sebagian tikus menempati lubang yang cukup dalam dan sebagian lagi banyak memilih tinggal di dalam perangkap bambu terutama pada saat bunting dan bermalai (Thamrin et al., 2001).\n" +
                            "    4. Sistem pagar perangkap\n" +
                            "Tikus menyenangi atau memilih padi fase generatif dari pada vegetatif, sehingga padi yang \tfase generatifnya lebih awal dari pada tanaman padi di sekitarnya akan mengalami kerusakan berat, karena semua populasi tikus yang ada di sekitar pertanaman akan memakan padi tersebut. Fenomena ini melahirkan teknik pengendalian tikus dengan menggunakan tanaman perangkap. Tanaman perangkap tersebut diberi pagar yang berlubang dan di dalamnya dilengkapi dengan perangkap bubu, sehingga tikus yang masuk melalui lubang tersebut akan terperangkap.\n" +
                            "    5. Pengumpanan beracun\n" +
                            "Pengumpanan beracun efektif bila tidak ada tanaman di lapang dan dapat dilakukan apabila ditemukan serangan >15%. Umpan diletakan pada tempat-tempat yang banyak dikunjungi \tatau dilewati tikus. Apabila umpan yang dipasang habis, berarti populasi tikus tinggi, perlu dilakukan pengumpanan ulang pada saat menjelang akhir anakan maksimal.\n" +
                            "    6. Pemanfaatan musuh alami\n" +
                            "Musuh alami hama tikus antara lain: kucing, anjing, ular sawah, dan burung hantu.\n" +
                            "    7. Lain-lain\n" +
                            "Penggenangan lahan, gropyokan, dan pemanfaatan jaring. Pengendalian hama tikus tidak dapat dilakukan hanya oleh sebagian petani. Pengendaliannya harus terorganisasi secara \tbaik dalam wilayah yang luas. Tanpa organisasi pengendalian yang baik maka teknologi \tpengendalian yang efektif tidak akan berhasil menekan populasi hama tikus. Sanitasi dan waktu tanam yang serentak adalah komponen pengendalian yang harus dilakukan oleh semua petani. Namun dalam mengatur setiap komponen pengendalian diperlukan adanya keterlibatan pengambil kebijakan yang bekerjasama dengan penanggung jawab teknis agar pengendalian dapat dikoordinasi dengan baik.");
                }else if (hasil.equals("wereng")){
                    gejala.setText("obat");
                    solusi.setText("hama");
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
