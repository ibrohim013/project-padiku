package com.jendral.padiku.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jendral.padiku.LoginActivity;
import com.jendral.padiku.R;
import com.jendral.padiku.DaftarActivity;
import com.jendral.padiku.model.dataDaftar;
import com.jendral.padiku.preferences;
import com.jendral.padiku.sheet.BottomSheet;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsFragment extends Fragment {
    Button daftar;
    Button masuk;
    Context context;
    RelativeLayout linearUdahLogin, linearBelumLogin;
    CircleImageView imageProfile;
    EditText nama,
            email,
            kota,
            telepon;

    String _nama,
            _email,
            _kota,
            _telepon,
            _gambar;
    Button ubahSandi;

    ArrayList<dataDaftar> ArrayListUdahDaftar = new ArrayList();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_notifications, container, false);
        context = v.getContext();
        daftar = v.findViewById(R.id.daftar);
        masuk = v.findViewById(R.id.masuk);
        linearUdahLogin = v.findViewById(R.id.linearUdahLogin);
        linearBelumLogin = v.findViewById(R.id.linearBelumLogin);
        nama = v.findViewById(R.id.nama);
        email = v.findViewById(R.id.email);
        kota = v.findViewById(R.id.kota);
        telepon = v.findViewById(R.id.telepon);
        imageProfile = v.findViewById(R.id.imageProfile);
        ubahSandi = v.findViewById(R.id.ubahSandi);

        if (preferences.getActiveData(v.getContext())) {
            linearBelumLogin.setVisibility(View.GONE);
            linearUdahLogin.setVisibility(View.VISIBLE);
            showDataUserDetail();
        } else {
            linearBelumLogin.setVisibility(View.VISIBLE);
            linearUdahLogin.setVisibility(View.GONE);

        }

        daftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, DaftarActivity.class));
            }
        });

        masuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, LoginActivity.class));
            }
        });

        ubahSandi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheet bottom_sheet = new BottomSheet();
                bottom_sheet.show(getChildFragmentManager(),"FM-SHOW");
            }
        });


        return v;
    }

    private void showDataUserDetail(){
        database.child("data-user").child(preferences.getNamaData(context))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!= null) {
                            _nama = dataSnapshot.child("nama").getValue(String.class);
                            _email = dataSnapshot.child("email").getValue(String.class);
                            _kota = dataSnapshot.child("kota").getValue(String.class);
                            _telepon = dataSnapshot.child("telepon").getValue(String.class);
                            _gambar = dataSnapshot.child("gambar").getValue(String.class);

                            nama.setText(_nama);
                            email.setText(_email);
                            kota.setText(_kota);
                            telepon.setText(_telepon);

                            Glide.with(context).load(_gambar).placeholder(R.drawable.profile).into(imageProfile);
    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

}
