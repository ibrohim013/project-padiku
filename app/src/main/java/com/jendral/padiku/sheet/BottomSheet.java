package com.jendral.padiku.sheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jendral.padiku.R;
import com.jendral.padiku.model.dataLogin;
import com.jendral.padiku.preferences;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BottomSheet extends BottomSheetDialogFragment {
    EditText sandi_sekarang,
    sandi_baru,
            konfirmasi_sandi;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    Button button_ubah_sandi;
    String sandi_now;
    Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.bottom_sheet, container,false);
        context = v.getContext();
        sandi_sekarang = v.findViewById(R.id.sandi_sekarang);
        sandi_baru = v.findViewById(R.id.sandi_baru);
        konfirmasi_sandi = v.findViewById(R.id.konfirmasi_sandi);
        button_ubah_sandi = v.findViewById(R.id.button_ubah_sandi);

        showData();

        button_ubah_sandi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                database.child("login").child(preferences.getNamaData(context)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataLogin logins = dataSnapshot.getValue(dataLogin.class);
                        String _sandi_sekarang = sandi_sekarang.getText().toString();
                        String _sandi_baru = sandi_baru.getText().toString();
                        String _konfirmasi_sandi = konfirmasi_sandi.getText().toString();
                        BCrypt.Result result = BCrypt.verifyer().verify(_sandi_sekarang.toCharArray(), logins.getPassword());
                        if (_sandi_sekarang.isEmpty()){
                            sandi_sekarang.setError("Data tidak boleh kosong");
                            sandi_sekarang.requestFocus();

                        }else if(_sandi_baru.isEmpty()){
                            sandi_baru.setError("Data tidak boleh kosong");
                            sandi_baru.requestFocus();

                        }else if(_konfirmasi_sandi.isEmpty()){
                            konfirmasi_sandi.setError("Data tidak boleh kosong");
                            konfirmasi_sandi.requestFocus();

                        }else if(!result.verified){
                            sandi_sekarang.setError("Sandi sekarang salah");
                            sandi_sekarang.requestFocus();
                        }else if (!_konfirmasi_sandi.equals(_sandi_baru)){
                            konfirmasi_sandi.setError("Konfirmasi sandi salah");
                            konfirmasi_sandi.requestFocus();
                        }else{
                            String bcryptHashString = BCrypt.withDefaults().hashToString(12, _sandi_baru.toCharArray());
                            database.child("login").child(preferences.getNamaData(context))
                                    .child("password").setValue(bcryptHashString).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context,"Sandi berhasil diubah",Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context,"Sandi gagal diubah",Toast.LENGTH_SHORT).show();
                                    dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        return v;
    }

    private void showData(){
        database.child("login").child(preferences.getNamaData(context)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sandi_now = dataSnapshot.child("password").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
