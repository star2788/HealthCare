package com.example.main;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Frag_stopwatch extends Fragment {

    private View view;
    private Button e_btn, n_btn, s_btn, bl_btn;
    private boolean ch_e, ch_n, ch_s;

    // 0 : 측정아닌상태, 1 : 일반모드, 2 : 수면모드, 3 : 운동모드

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_stopwatch , container , false);

        e_btn = view.findViewById(R.id.eb);
        s_btn = view.findViewById(R.id.sb);
        n_btn = view.findViewById(R.id.nb);
        bl_btn = view.findViewById(R.id.button2); //근전도 베이스라인 측정버튼
        ch_e = ch_n = ch_s = true;


        e_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //운동모드
                if(ch_e == true){
                   BluetoothService.mode = 3;
                    ch_e = false;
                }
                else{
                    BluetoothService.mode = 1;
                }
            }
        });

        s_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ch_s == true){
                    BluetoothService.mode = 2;
                    ch_s = false;
                }
                else{
                    BluetoothService.mode = 1;

                }

            }
        });

        n_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ch_n == true){
                    BluetoothService.mode = 1;
                    ch_n = false;
                }
                else{
                    BluetoothService.mode = 0;
                }
            }
        });

        bl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothService.mode = 0;
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                musclebl1Fragment musclebl1fragment = new musclebl1Fragment();
                transaction.replace(R.id.main_layout,musclebl1fragment);
                transaction.commit();
            }
        });
        return view;
    }

}
