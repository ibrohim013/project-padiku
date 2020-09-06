package com.jendral.padiku.adapter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jendral.padiku.DetailActivity;
import com.jendral.padiku.R;
import com.jendral.padiku.model.dataGejala;
import com.jendral.padiku.preferences;
import com.jendral.padiku.sheet.dialogGejala;

import java.util.ArrayList;

public class AdapterGejala extends RecyclerView.Adapter<AdapterGejala.GejalaViewHolder> {
    Context context;
    ArrayList<dataGejala> gejalaArrayList;
    Fragment fragment;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    public AdapterGejala(Context context, ArrayList<dataGejala> gejalaArrayList, Fragment fragment) {
        this.context = context;
        this.gejalaArrayList = gejalaArrayList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public GejalaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hama_gejala, parent, false);
        return new GejalaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GejalaViewHolder holder, int position) {
        holder.viewBind(gejalaArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return gejalaArrayList.size();
    }

    public class GejalaViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_gejala;
        TextView txt_judul;

        public GejalaViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_gejala = itemView.findViewById(R.id.iv_gejala);
            txt_judul = itemView.findViewById(R.id.txt_judul);
        }

        public void viewBind(final dataGejala dataGejala) {
            Glide.with(context).load(dataGejala.getGambar()).placeholder(R.drawable.image_default).into(iv_gejala);
            txt_judul.setText(dataGejala.getJudul());

            if(preferences.getLevelData(context).equals("admin")){
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        AlertDialog.Builder dialog2 = new AlertDialog.Builder(context);
                        dialog2.setItems(new CharSequence[]{"Hapus", "Ubah"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogss, int which) {
                                switch (which) {
                                    case 0:
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                                        dialog.setMessage("Apa kamu yakin ingin hapus data ini ?")
                                                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        database.child("gejala").child(dataGejala.getKey()).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(context, "Data berhasil di hapus", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });

                                                    }
                                                }).setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                        break;
                                    case 1:
                                        FragmentManager fragmentManager = fragment.getChildFragmentManager();
                                        dialogGejala gejalas = new dialogGejala(context, "ubah", dataGejala);
                                        gejalas.show(fragmentManager, "dialog-gejala");

                                        break;
                                }
                            }
                        }).show();

                        return true;
                    }

                });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("gambar", dataGejala.getGambar());
                    intent.putExtra("title", dataGejala.getJudul());
                    intent.putExtra("hasil_gejala", dataGejala.getGejala());
                    intent.putExtra("hasil_solusi", dataGejala.getSolusi());
                    context.startActivity(intent);
                }
            });
        }
    }
}
