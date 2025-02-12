package com.example.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class musclebl1Fragment extends Fragment {

    private static final int MUSCLE_READY = 0, MUSCLE_END = 1;
    private static int strength_avg_L = 0, strength_L = 500, muscle_avg_L = 0, strength_avg_R = 0, strength_R = 500, muscle_avg_R = 0;
    private int i = 0, j = 0, relax_avg_L = 0,relax_avg_R = 0;
    SharedPreferences auto;
    SharedPreferences.Editor editor;
    private String str = "0";
    private String muscle_L = "0", muscle_R = "0";
    private Frag_main frag_main;


    private Handler muscleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MUSCLE_READY:
                    startListening();

                    break;
                case MUSCLE_END: {
                    muscleHandler.removeCallbacksAndMessages(null);
                    break;
                }

                default:
                    super.handleMessage(msg);
            }

        }
    };
    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musclebl1, container, false);

        relax_avg_L = getArguments().getInt("relax_avg_L");
        relax_avg_R = getArguments().getInt("relax_avg_R");
        textView = view.findViewById(R.id.textView19);


        muscleHandler.sendEmptyMessage(MUSCLE_READY);

        return view;
    }


    private void startListening() {
        strength_L = ATOI(muscle_L);
        strength_R = ATOI(muscle_R);
        textView.setText("L" + strength_L + "R" + strength_R);
        if (strength_L > 200 && strength_R>200) {
            strength_avg_L += strength_L;
            strength_avg_R += strength_R;
            i++;
        } else {
            j++;
        }

        if (j == 5) {
            Toast.makeText(getContext(), "다시 시도해주세요", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, ((MainActivity) getActivity()).get_frag_main()).commit();
            muscleHandler.sendEmptyMessage(MUSCLE_END);
        } else if (i == 5) {
            strength_avg_L = (int) (strength_avg_L / 5);
            strength_avg_R = (int) (strength_avg_R / 5);
            muscle_avg_L = (int) ((strength_avg_L + relax_avg_L) / 2);
            muscle_avg_R = (int) ((strength_avg_R + relax_avg_R) / 2);
            ((MainActivity) getActivity()).get_frag_main().setMuscle_avg(muscle_avg_L, muscle_avg_R);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, ((MainActivity) getActivity()).get_frag_main()).commit();
            muscleHandler.sendEmptyMessage(MUSCLE_END);
        } else {
            muscleHandler.sendEmptyMessageDelayed(MUSCLE_READY, 1000);
        }

    }


    public static int ATOI(String sTmp) {
        String tTmp = "0", cTmp = "";

        sTmp = sTmp.trim();
        for (int i = 0; i < sTmp.length(); i++) {
            cTmp = sTmp.substring(i, i + 1);
            if (cTmp.equals("0") ||
                    cTmp.equals("1") ||
                    cTmp.equals("2") ||
                    cTmp.equals("3") ||
                    cTmp.equals("4") ||
                    cTmp.equals("5") ||
                    cTmp.equals("6") ||
                    cTmp.equals("7") ||
                    cTmp.equals("8") ||
                    cTmp.equals("9")) tTmp += cTmp;
            else if (cTmp.equals("-") && i == 0)
                tTmp = "-";
            else
                break;
        }

        return (Integer.parseInt(tTmp));
    }

    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(bluetoothReceiver,
                new IntentFilter("com.example.action.bluetooth.receive"));
    }

    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(bluetoothReceiver);
    }


    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            muscle_L=intent.getStringExtra("Muscle_L");
            muscle_R=intent.getStringExtra("Muscle_R");
            //strength = Integer.parseInt(intent.getStringExtra("Muscle"));

        }
    };


}