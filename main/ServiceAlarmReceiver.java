package com.example.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("재실행 _ 음성");

            Intent in = new Intent(context, RestartRecognition.class);
            in.putExtra("userID",intent.getStringExtra("userID"));
            in.putExtra("docName",intent.getStringExtra("docName"));
            in.putExtra("docID",intent.getStringExtra("docID"));
            context.startForegroundService(in);
        } else {
            Intent in = new Intent(context, Recognition.class);
            in.putExtra("userID",intent.getStringExtra("userID"));
            in.putExtra("docName",intent.getStringExtra("docName"));
            in.putExtra("docID",intent.getStringExtra("docID"));

            context.startService(in);
        }
    }

}

