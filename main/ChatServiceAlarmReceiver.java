package com.example.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ChatServiceAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent in = new Intent(context, RestartChatService.class);
            in.putExtra("userID",intent.getStringExtra("userID"));
            in.putExtra("docName",intent.getStringExtra("docName"));
            in.putExtra("conDoc",intent.getStringExtra("conDoc"));
            System.out.println("재실행 _ 챗");
            context.startForegroundService(in);
        } else {
            Intent in = new Intent(context, ChatService.class);
            in.putExtra("userID",intent.getStringExtra("userID"));
            in.putExtra("docName",intent.getStringExtra("docName"));
            in.putExtra("conDoc",intent.getStringExtra("conDoc"));
            context.startService(in);
        }
    }

}

