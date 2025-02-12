package com.example.main;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

//참고 : https://always-21.tistory.com/4
public class AlarmReceiver extends BroadcastReceiver {

    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";
    private NotificationManager manager;
    private NotificationCompat.Builder builder;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        builder = null;
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE){
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT));
            builder = new NotificationCompat.Builder(context,CHANNEL_ID);
        }else{
            builder = new NotificationCompat.Builder(context);
        }
        Intent intent2 = new Intent(context,PopupActivity.class);
        intent2.putExtra("res_number", "20210007");
        intent2.putExtra("hospital", "삼성 서울병원");
        intent2.putExtra("doc", "한의사");
        intent2.putExtra("date", "2021/3/24");
        intent2.putExtra("time", "13:30");
        intent2.putExtra("room", "5B");
        PendingIntent pendingIntent = PendingIntent.getActivity(context,101,intent2,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentTitle("예약 알림");
        builder.setAutoCancel(true);
        builder.setContentText("2021년 3월 25일 오전 10시 30분");
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        manager.notify(1,notification);
    }


}