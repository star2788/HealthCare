package com.example.main;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.collection.ArraySet;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.ChatRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ChatService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private static ArrayList<ChatData> mList;
    public static int last_number;
    private ChatRequest chatReceiveRequest;
    private RequestQueue requestQueue;
    private static final int MSG_CHAT_RECEIVE_READY = 0;
    private static final int MSG_CHAT_RECEIVE_END = 1;
    private static final int MSG_CHAT_RECEIVE_RESTART = 2;
    private String userID = "";
    private String conDoc = "";
    private String docName = "";
    private int cmp = 0;
    public static Intent serviceIntent = null;
    //푸시알람
    private static String CHANNEL_ID = "channel2";
    private static String CHANNEL_NAME = "Channel2";
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private String e_id = "";
    private String e_msg = "";
    private String e_date = "";
    private String e_sender = "";
    private String e_receiver = "";
    private AES256Chiper aes256Chiper = new AES256Chiper();


    private Response.Listener<String> responseListener1 = new Response.Listener<String>() { //1초씩 반복해서 메세지 받아오는 부분
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResponse(String response) {
            if (response != null)
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    boolean success = jsonObject.getBoolean("success");
                    if (success) {
                        last_number = jsonObject.getInt("number");
                        String msg = jsonObject.getString("msg");
                        String date = jsonObject.getString("date");
                        String sender = jsonObject.getString("sender");
                        String receiver = jsonObject.getString("receiver");

                        e_msg = aes256Chiper.decoding(msg,MainActivity.key);
                        e_date = aes256Chiper.decoding(date,MainActivity.key);
                        e_sender = aes256Chiper.decoding(sender, MainActivity.key);
                        e_receiver = aes256Chiper.decoding(receiver, MainActivity.key);


                        if (e_sender.equals(conDoc)) {
                            e_sender = docName;
                        }


                        if (!(e_sender.equals(userID)) && cmp != last_number) { // 리스트에 저장된 메세지 중복 방지

                            if(e_msg.contains("피드백이 도착했습니다")){
                                Requestalarm(date);
                            };
                            mList.add(new ChatData(e_msg, e_date, e_sender, e_receiver));
                        } else {
                            Log.v("채팅", "cmp == lastnum");
                        }
                        cmp = last_number;

                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
        }
    };


    public class LocalBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }//초기화

    private Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHAT_RECEIVE_READY:
                    break;
                case MSG_CHAT_RECEIVE_END: {
                    stopListening();
                    sendEmptyMessageDelayed(MSG_CHAT_RECEIVE_RESTART, 1000);
                    break;
                }
                case MSG_CHAT_RECEIVE_RESTART:
                    try {
                        startListening();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidAlgorithmParameterException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    private void stopListening() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startListening() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        handler.sendEmptyMessage(MSG_CHAT_RECEIVE_END);
        // 라스트넘버를 기준으로 그 이상값의 데이터가 있으면 가져오고 핸들러에 엔드값전송
/*
        builder = null;
        manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE) {
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("자세가 불균형합니다.")
                .setContentText("바른 자세를 유지해 주세요.")
                .setDefault4e5336s(Notification.DEFAULT_VIBRATE)
                .setAutoCancel(true);

        Notification notification = builder.build();
        manager.notify(1,notification);*/


        String id = aes256Chiper.encoding(userID,MainActivity.key);
        chatReceiveRequest = new ChatRequest(last_number, id, responseListener1);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(chatReceiveRequest);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//시작

        userID = intent.getStringExtra("userID");
        conDoc = intent.getStringExtra("conDoc");
        docName = intent.getStringExtra("docName");
        System.out.println("채팅 인텐트 : "+userID+" / "+conDoc+" / "+docName);


        serviceIntent = intent;
        sendNotification();

        Response.Listener<String> responseListener2 = new Response.Listener<String>() { // 처음에 메세지 받아오는 부분
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {
                try {
                    mList = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(response);
                    int i = 0;
                    while (i < jsonArray.length()) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            last_number = jsonObject.getInt("number");  //마지막채팅의 번호 받아서 이후로는 이번호이상 채팅 반복문으로 긁어옴. 긁어올때마다 번호증가
                            String msg = jsonObject.getString("msg");
                            String date = jsonObject.getString("date");
                            String sender = jsonObject.getString("sender");
                            String receiver = jsonObject.getString("receiver");


                            e_msg = aes256Chiper.decoding(msg,MainActivity.key);
                            e_date = aes256Chiper.decoding(date,MainActivity.key);
                            e_sender = aes256Chiper.decoding(sender, MainActivity.key);
                            e_receiver = aes256Chiper.decoding(receiver, MainActivity.key);

                            if (e_sender.equals(conDoc)) {
                                e_sender = docName;
                            }

                            System.out.println("chatting : 처음 가져올 때 애드 : "+mList);
                            mList.add(new ChatData(e_msg, e_date, e_sender, e_receiver));
                        }
                        i++;
                    }
                    startListening();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        try {
            String e_id = aes256Chiper.encoding(userID,MainActivity.key);
            chatReceiveRequest = new ChatRequest(0, e_id, responseListener2);
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(chatReceiveRequest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }



        return START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDestroy() {
        super.onDestroy();
        serviceIntent = null;
        //setAlarmTimer();


    }//종료

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, ChatServiceAlarmReceiver.class);
        intent.putExtra("userID", userID);
        intent.putExtra("docName", docName);
        intent.putExtra("conDoc", conDoc);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    private void sendNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = "fcm_default_channel1";//getString(R.string.default_notification_channel_id);
        //Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "2")
                        .setSmallIcon(R.mipmap.ic_launcher)//drawable.splash)
                        .setContentTitle(null)
                        .setContentText(null)
                        .setOngoing(true);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText("설정을 보려면 누르세요");
        style.setBigContentTitle(null);
        style.setSummaryText("서비스 동작중");

        notificationBuilder.setStyle(style);
        notificationBuilder.setWhen(0);
        notificationBuilder.setShowWhen(false);
        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("2","Chat Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        //notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        Notification notification = notificationBuilder.build();
        startForeground(2,notification);

    }




    public static ArrayList<ChatData> returnChatList() {
        return mList;
    }

    public void Requestalarm(String date) {
        Intent intent = new Intent("com.example.action.chatservice");
        intent.putExtra("alarm_request", date);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

}