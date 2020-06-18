package com.jendral.padiku.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jendral.padiku.LoginActivity;
import com.jendral.padiku.R;
import com.jendral.padiku.DaftarActivity;
import com.jendral.padiku.preferences;

public class NotificationsFragment extends Fragment {
    Button daftar;
    Button masuk;
    Context context;
    RelativeLayout linearUdahLogin,linearBelumLogin;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_notifications, container, false);
        context = v.getContext();
        daftar = v.findViewById(R.id.daftar);
        masuk = v.findViewById(R.id.masuk);
        linearUdahLogin = v.findViewById(R.id.linearUdahLogin);
        linearBelumLogin = v.findViewById(R.id.linearBelumLogin);

        if(preferences.getActiveData(v.getContext())){
            linearBelumLogin.setVisibility(View.GONE);
            linearUdahLogin.setVisibility(View.VISIBLE);
        }else{
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

        return v;
    }
}
