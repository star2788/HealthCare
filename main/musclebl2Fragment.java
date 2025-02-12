package com.example.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
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

import java.util.Random;

public class musclebl2Fragment extends Fragment {

    private static final int MUSCLE_READY = 0, MUSCLE_END = 1;
    private static int relax_avg_L = 0, relax_L = 0,relax_avg_R = 0, relax_R = 0;
    private int i = 0, j = 0;
    private static String str = "0";
    private String muscle_L = "0", muscle_R = "0";
    SharedPreferences auto;
    SharedPreferences.Editor editor;
    private musclebl1Fragment musclebl1Fragment;
    private Frag_main frag_main;
    private TextView textView;


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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_musclebl2, container, false);
        musclebl1Fragment = new musclebl1Fragment();
        textView = view.findViewById(R.id.textView19);

        muscleHandler.sendEmptyMessage(MUSCLE_READY);


        return view;
    }


    private void startListening() {

        relax_L = ATOI(muscle_L);
        relax_R = ATOI(muscle_R);
        textView.setText("L" + relax_L + "R" + relax_R);


        if (relax_L < 100 && relax_R < 100) {
            relax_avg_L += relax_L;
            relax_avg_R += relax_R;
            i++;
        } else {
            j++;
        }
        if (j == 5) {
            Toast.makeText(getContext(), "다시 시도해주세요", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, ((MainActivity) getActivity()).get_frag_main()).commit();
            muscleHandler.sendEmptyMessage(MUSCLE_END);
        } else if (i == 5) {
            relax_avg_L /= 5;
            relax_avg_R /= 5;
            Bundle bundle = new Bundle();
            bundle.putInt("relax_avg_L", relax_avg_L);
            bundle.putInt("relax_avg_R", relax_avg_R);
            musclebl1Fragment.setArguments(bundle);

            Toast.makeText(getContext(), "다음 단계로 진행합니다", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, musclebl1Fragment).commit();
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

            //relax = Integer.parseInt(intent.getStringExtra("Muscle"));

        }
    };
}