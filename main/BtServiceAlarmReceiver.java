package com.example.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BtServiceAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent in = new Intent(context, BluetoothService.class);
            in.putExtra("userID",intent.getStringExtra("userID"));
            in.putExtra("userName",intent.getStringExtra("userName"));
            in.putExtra("docID",intent.getStringExtra("docID"));
            context.startForegroundService(in);
        } else {
            Intent in = new Intent(context, BluetoothService.class);
            in.putExtra("userID",intent.getStringExtra("userID"));
            in.putExtra("userName",intent.getStringExtra("userName"));
            in.putExtra("docID",intent.getStringExtra("docID"));
            context.startService(in);
        }
    }
}
