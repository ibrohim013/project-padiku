package com.jendral.padiku.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jendral.padiku.R;
import com.jendral.padiku.model.dataCommunication;
import com.jendral.padiku.preferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class CommunicationRecyclerAdapter extends RecyclerView.Adapter<CommunicationRecyclerAdapter.CommunicationViewHolder> {
    Context context;
    List<dataCommunication> listCommunication;
    List<String> listData;
    Toolbar toolbar;

    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    StorageReference storage = FirebaseStorage.getInstance().getReference();

    public CommunicationRecyclerAdapter(Context context, List<dataCommunication> listCommunication, List<String> listData, Toolbar toolbar) {
        this.context = context;
        this.listCommunication = listCommunication;
        this.listData = listData;
        this.toolbar = toolbar;
    }

    @NonNull
    @Override
    public CommunicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_communication, parent, false);
        return new CommunicationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommunicationViewHolder holder, int position) {
        String itemData = listData.get(position);
        dataCommunication itemNormal = listCommunication.get(position);

        Date date = new Date();
        Locale locale = new Locale("in", "ID");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMMM/yyyy", locale);
        long kemarin = date.getTime() - (1000 * 60 * 60 * 24);

        if (itemNormal.getTanggal().equals(itemData)) {
            holder.linearTanggal.setVisibility(View.GONE);
        }

        if (itemNormal.getTanggal().equals(simpleDateFormat.format(kemarin))) {
            holder.textTanggal.setText("Kemarin");
        } else if (itemNormal.getTanggal().equals(simpleDateFormat.format(date.getTime()))) {
            holder.textTanggal.setText("Sekarang");
        } else {
            holder.textTanggal.setText(itemNormal.getTanggal());
        }
        holder.bindView(listCommunication.get(position));
    }

    @Override
    public int getItemCount() {
        return listCommunication.size();
    }

    public class CommunicationViewHolder extends RecyclerView.ViewHolder {
        TextView dari,
                pesan,
                waktu,
                textTanggal,level;
        CircleImageView imageView;
        LinearLayout linear, linear2, linearTanggal;
        CardView cardView, cardTanggal;
        ImageView imageMessage;

        public CommunicationViewHolder(@NonNull View itemView) {
            super(itemView);
            dari = itemView.findViewById(R.id.dari);
            imageMessage = itemView.findViewById(R.id.imageMessage);
            pesan = itemView.findViewById(R.id.pesan);
            waktu = itemView.findViewById(R.id.waktu);
            level = itemView.findViewById(R.id.level);
            imageView = itemView.findViewById(R.id.imageView);
            linear = itemView.findViewById(R.id.linear);
            linear2 = itemView.findViewById(R.id.linear2);
            cardView = itemView.findViewById(R.id.cardView);
            linearTanggal = itemView.findViewById(R.id.linearTanggal);
            cardTanggal = itemView.findViewById(R.id.cardTanggal);
            textTanggal = itemView.findViewById(R.id.textTanggal);

        }

        public void bindView(final dataCommunication dataCommunication) {
            Locale locale = new Locale("in", "ID");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm aa", locale);

            dari.setText(dataCommunication.getDari());
            level.setText(dataCommunication.getLevel());
            pesan.setText(dataCommunication.getPesan());
            waktu.setText(simpleDateFormat.format(dataCommunication.getWaktu()));

            database.child("login").child(dataCommunication.getKey()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String srcImage = dataSnapshot.child("image").getValue(String.class);
                    Glide.with(context).load(srcImage).placeholder(R.drawable.profile).into(imageView);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if (dataCommunication.getJenis().equals("image")) {
                pesan.setVisibility(View.GONE);
                imageMessage.setVisibility(View.VISIBLE);
                Glide.with(context).load(dataCommunication.getPesan()).placeholder(R.drawable.profile).into(imageMessage);
            } else if (dataCommunication.getJenis().equals("text")) {
                pesan.setVisibility(View.VISIBLE);
                imageMessage.setVisibility(View.GONE);
            }

            if (dataCommunication.getKey().equals(preferences.getKeyData(context))) {
                dari.setVisibility(View.GONE);
                level.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
                linear.setGravity(Gravity.CENTER | Gravity.END);
                linear2.setGravity(Gravity.CENTER | Gravity.END);

                pesan.setTextColor(context.getResources().getColor(android.R.color.white));
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimaryDark));


            }

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if (dataCommunication.getKey().equals(preferences.getKeyData(context))) {
                        Date dateNow = new Date();
                        long limit = dateNow.getTime() - dataCommunication.getWaktu();

                        if (dataCommunication.getJenis().equals("text")) {
                            toolbarHiden();
                            if (limit <= (1000 * 60 * 5)) {
                                menuLimitShowText(dataCommunication.getPush(), dataCommunication.getPesan());

                            } else {
                                menuNotLimitShowText(dataCommunication.getPesan());
                            }

                        } else {

                            if (limit <= (1000 * 60 * 5)) {

                                menuLimitShowData(
                                        dataCommunication.getPush(),
                                        dataCommunication.getJenis(),
                                        dataCommunication.getImage()
                                );

                            } else {

                                menuNotLimitShowData(
                                        dataCommunication.getJenis(),
                                        dataCommunication.getImage()
                                );

                            }
                        }
                    } else {

                        if (dataCommunication.getJenis().equals("text")) {
                            menuNotLimitShowText(dataCommunication.getPesan());

                        } else {
                            menuNotLimitShowData(
                                    dataCommunication.getJenis(),
                                    dataCommunication.getImage()
                            );
                        }

                    }

                    return true;
                }
            });

        }
    }

    private void menuNotLimitShowData(String jenis, String image) {
        toolbarHiden();
        toolbar.getMenu().findItem(R.id.close).setVisible(true);
        toolbar.getMenu().findItem(R.id.download).setVisible(true);

        setMenuClose();
        setMenuDownload(jenis, image);
    }

    private void menuLimitShowData(String push, String jenis, String image) {
        toolbarHiden();
        toolbar.getMenu().findItem(R.id.close).setVisible(true);
        toolbar.getMenu().findItem(R.id.download).setVisible(true);
        toolbar.getMenu().findItem(R.id.delete).setVisible(true);

        setMenuClose();
        setMenuDownload(jenis, image);
        setDeleteMenuImage(push, image, jenis);
    }

    private void menuNotLimitShowText(String pesan) {
        toolbarHiden();

        toolbar.getMenu().findItem(R.id.close).setVisible(true);
        toolbar.getMenu().findItem(R.id.copy).setVisible(true);

        setMenuClose();
        setCopyMessage(pesan);

    }

    private void menuLimitShowText(String push, String pesan) {
        toolbarHiden();

        toolbar.getMenu().findItem(R.id.close).setVisible(true);
        toolbar.getMenu().findItem(R.id.copy).setVisible(true);
        toolbar.getMenu().findItem(R.id.delete).setVisible(true);

        setMenuClose();
        setDeleteMessage(push);
        setCopyMessage(pesan);
    }

    private void setCopyMessage(final String pesan) {
        toolbar.getMenu().findItem(R.id.copy).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("message", pesan);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(context, "Pesan berhasil disalin", Toast.LENGTH_SHORT).show();
                toolbarHiden();
                return true;
            }
        });
    }

    private void setDeleteMessage(final String push) {
        toolbar.getMenu().findItem(R.id.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Apakah yakin ingin menghapus pesan ini?")
                        .setPositiveButton("YA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                database.child("pesan").child(push).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                        Toast.makeText(context, "Pesan berhasil terhapus", Toast.LENGTH_SHORT).show();
                                        toolbarHiden();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        toolbarHiden();
                                    }
                                });
                            }
                        }).setNegativeButton("TIDAK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                return true;
            }
        });
    }

    private void setDeleteMenuImage(final String push, final String image, final String jenis) {
        toolbar.getMenu().findItem(R.id.delete).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage("Apakah yakin ingin menghapus pesan ini?")
                        .setPositiveButton("YA", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                storage.child("media").child(jenis).child(image).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        database.child("pesan").child(push).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                dialog.dismiss();
                                                Toast.makeText(context, "Pesan berhasil terhapus", Toast.LENGTH_SHORT).show();
                                                toolbarHiden();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                toolbarHiden();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        toolbarHiden();
                                    }
                                });
                            }
                        }).setNegativeButton("TIDAK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                return true;
            }
        });
    }

    private void setMenuDownload(final String jenis, final String image) {
        toolbar.getMenu().findItem(R.id.download).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                StorageReference strg = storage.child("media").child(jenis).child(image);
                strg.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(uri);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, image);
                        downloadManager.enqueue(request);
                        Toast.makeText(context, "Gambar berhasil didownload", Toast.LENGTH_SHORT).show();

                        toolbarHiden();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        toolbarHiden();
                    }
                });
                return true;
            }
        });
    }

    private void setMenuClose() {
        toolbar.getMenu().findItem(R.id.close).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                toolbarHiden();

                return true;
            }
        });
    }

    private void toolbarHiden() {
        toolbar.getMenu().findItem(R.id.download).setVisible(false);
        toolbar.getMenu().findItem(R.id.close).setVisible(false);
        toolbar.getMenu().findItem(R.id.copy).setVisible(false);
        toolbar.getMenu().findItem(R.id.delete).setVisible(false);


    }
}
