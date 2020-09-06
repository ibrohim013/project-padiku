package com.jendral.padiku.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jendral.padiku.R;
import com.jendral.padiku.adapter.AdapterGejala;
import com.jendral.padiku.model.dataGejala;
import com.jendral.padiku.preferences;
import com.jendral.padiku.sheet.dialogGejala;

import java.util.ArrayList;


public class HamaGejalaFragment extends Fragment {
    FloatingActionButton fab_add;
    RecyclerView recyclerView;
    Context context;
    AdapterGejala adapterGejala;
    ArrayList<dataGejala> gejalaArrayList = new ArrayList<>();
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_hama_gejala, container, false);
        fab_add = v.findViewById(R.id.fab_add);
        recyclerView = v.findViewById(R.id.recyclerView);
        context = v.getContext();
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);

        if (preferences.getLevelData(context).equals("admin")){
            fab_add.setVisibility(View.VISIBLE);
        }else {
            fab_add.setVisibility(View.GONE);
        }


        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogGejala gejalas = new dialogGejala(context,"tambah");
                gejalas.show(getChildFragmentManager(),"dialog-gejala");
            }
        });


        showData();
        return v;
    }

    private void showData() {
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        database.child("gejala").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                gejalaArrayList.clear();
                for (DataSnapshot item : dataSnapshot.getChildren()){
                    dataGejala item_gejala = item.getValue(dataGejala.class);
                    if (item_gejala!= null){
                        item_gejala.setKey(item.getKey());
                        gejalaArrayList.add(item_gejala);
                    }

                }
                adapterGejala = new AdapterGejala(context,gejalaArrayList, HamaGejalaFragment.this);
                recyclerView.setAdapter(adapterGejala);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}