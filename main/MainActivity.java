package com.example.main;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.MemberRequest;
import com.example.main.interfaces.DialogflowBotReply;
import com.example.main.utils.SendMessageInBg;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements DialogflowBotReply {
    Intent SttIntent;
    TextToSpeech tts;
    private String msg, rsp;
    SpeechRecognizer mRecognizer;
    TextView button;
    public static Context mContext;
    private DrawerLayout mDrawerLayout;
    private Context context = this; //자신이 어떤 애플리케이션을 나타내고 있는지 알려주는 ID역할
    private BottomNavigationView bottomNavigationView; //바텀 네비게이션 뷰
    private Frag_main frag_main;
    private Frag_calendar frag_calendar;
    private Frag_chat frag_chat;
    private Frag_data frag_data;
    private Frag_stopwatch frag_stopwatch;
    private ModifyFragment modifyFragment;
    private ConfirmFragment confirmFragment;
    private FeedbackFragment fbackFragment;
    private CheckmdfFragment checkmdfFragment;
    private checkpw_cdFragment checkpwCdFragment;
    private musclebl1Fragment muscleFragment;
    private TextView titleview, transmodify_tf, transfeedback_tf, transbooking_tf, logout_tf,msgcnt_tf, condoc_tf;
    private MenuItem bottomitem;
    private Bundle mBundle, mBundle2, msgBundle;
    public  String userID, userPW, userName, conDocID , docName, age;
    public static Intent recog_intent, chat_intent,bluetooth_intent;
    private String username, userbirth, userblood, userGender,
            userphone, useremail, useraddress1, useraddress2, userop, userfd, usertm, doc;
    private long backKeyPressedTime = 0;
    private Toast toast;
    private LinearLayout linear1, linear2, linear3, linear4;
    private ChatService mService;
    private static int array_length = 0;
    private int diff = 0;
    private boolean ch_page = false;
    private ChatThread chatThread;
    public static ArrayList<ChatData> chatDataArrayList;
    private AlarmManager alarmManager;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private String uuid = UUID.randomUUID().toString();
    private static String TAG = "tag";
    private BluetoothService bluetoothService;
    private CharSequence[] items = null;
    private ProgressDialog customProgressDialog;
    public static boolean checkreceiver = false;
    final static String key = "qwertyuiopasdfghjklzxcvbnmqwerty";
    private boolean check_msg = true;

    private ServiceConnection bluetoothConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private final Handler handler_V = new Handler()
    {
        public void handleMessage(Message msg)
        {
            msgcnt_tf.setVisibility(View.VISIBLE);
            msgcnt_tf.setText(Integer.toString(diff));
        }
    };


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ChatService.LocalBinder binder = (ChatService.LocalBinder) service;
            mService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //?

        mContext = this;
        setUpBot();
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.show();
        customProgressDialog.setCancelable(false);

        Toolbar toolbar = findViewById(R.id.toolbar);       //앱 상단의 앱바 중 하나(액션바와 툴바) 기본은 액션바로 지정.액션바는 커스터마이징불가
        setSupportActionBar(toolbar);                       //현재 액션바를 툴바로 대체
        ActionBar actionBar = getSupportActionBar();        //지정된 액션바를 가져옴 ( 툴바로 대체됐으니 반환값은 툴바)
        actionBar.setDisplayShowCustomEnabled(true);        // 툴바 커스텀 허용
        actionBar.setDisplayShowTitleEnabled(false);        // 타이틀사용여부
        actionBar.setDisplayHomeAsUpEnabled(true);          //자동 뒤로가기 버튼
        actionBar.setHomeAsUpIndicator(R.drawable.menu_24); // 뒤로가기 버튼의 아이콘

        button = findViewById(R.id.title);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mBundle = new Bundle();

        Intent thisIntent = getIntent();
        userName = thisIntent.getStringExtra("userName");
        String blood = thisIntent.getStringExtra("userBlood");
        String birth = thisIntent.getStringExtra("userBirth");
        userID = thisIntent.getStringExtra("userID");
        age = Integer.toString(calAge(birth));
        userPW = thisIntent.getStringExtra("userPW");
        conDocID = thisIntent.getStringExtra("conDoc");
        docName = thisIntent.getStringExtra("docName");
        String test = thisIntent.getStringExtra("userMBL");


        mBundle.putString("Name", userName);
        mBundle.putString("Blood", blood);
        mBundle.putString("Age", age);
        mBundle.putString("userID", userID);

        frag_main = new Frag_main();
        frag_main.setArguments(mBundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_main).commit();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        frag_calendar = new Frag_calendar();
        frag_data = new Frag_data();
        frag_stopwatch = new Frag_stopwatch();
        frag_chat = new Frag_chat();
        titleview = (TextView) findViewById(R.id.title);
        bottomNavigationView = findViewById(R.id.main_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                item.setCheckable(true);
                bottomitem = item;
                switch (item.getItemId()) {
                    case R.id.navigation_1:
                        ch_page = false;
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_main).commit();
                        titleview.setText("HEALTHCARE");
                        setSideitem(linear1, linear2, linear3, linear4);
                        break;
                    case R.id.navigation_2:
                        ch_page = false;
                        replaceFragment(frag_calendar);
                        titleview.setText("병원 예약");
                        setSideitem(linear1, linear2, linear3, linear4);
                        break;
                    case R.id.navigation_3:
                        ch_page = false;
                        replaceFragment(frag_data);
                        titleview.setText("건강 데이터");
                        setSideitem(linear1, linear2, linear3, linear4);
                        break;
                    case R.id.navigation_4:
                        ch_page = false;
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_stopwatch).commit();
                        titleview.setText("데이터 측정");
                        setSideitem(linear1, linear2, linear3, linear4);
                        break;
                    case R.id.navigation_5:
                        ch_page = true;
                        frag_chat.setArguments(msgBundle);
                        msgcnt_tf.setVisibility(View.INVISIBLE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_chat).commit();
                        titleview.setText("채팅 상담");
                        setSideitem(linear1, linear2, linear3, linear4);
                        break;
                }
                return true;
            }
        });

        modifyFragment = new ModifyFragment();
        confirmFragment = new ConfirmFragment();
        fbackFragment = new FeedbackFragment();
        checkmdfFragment = new CheckmdfFragment();
        checkpwCdFragment = new checkpw_cdFragment();
        muscleFragment = new musclebl1Fragment();
        transbooking_tf = (TextView) findViewById(R.id.mn_conres);
        transfeedback_tf = (TextView) findViewById(R.id.mn_confeed);
        transmodify_tf = (TextView) findViewById(R.id.mn_modify);
        condoc_tf = (TextView) findViewById(R.id.con_doc);

        logout_tf = (TextView) findViewById(R.id.mn_logout);
        linear1 = (LinearLayout) findViewById(R.id.linear1);
        linear2 = (LinearLayout) findViewById(R.id.linear2);
        linear3 = (LinearLayout) findViewById(R.id.linear3);
        linear4 = (LinearLayout) findViewById(R.id.linear4);

        transmodify_tf.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                titleview.setText("내 정보 수정");
                mDrawerLayout.closeDrawers();
                setbitem();
                ch_page = false;
                linear1.setBackgroundColor(context.getResources().getColor(R.color.touched_btn));
                setSideitem(linear2, linear3, linear3, linear4);
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                username = jsonObject.getString("userName");
                                userbirth = jsonObject.getString("userBirth");
                                userGender = jsonObject.getString("userGender");
                                useraddress1 = jsonObject.getString("userAddress1");
                                useraddress2 = jsonObject.getString("userAddress2");
                                userphone = jsonObject.getString("userPhone");
                                useremail = jsonObject.getString("userEmail");
                                userblood = jsonObject.getString("userBlood");
                                userop = jsonObject.getString("userOp");
                                userfd = jsonObject.getString("userFd");
                                usertm = jsonObject.getString("userTm");
                                doc = jsonObject.getString("conDoc");

                                mBundle2 = new Bundle();
                                mBundle2.putString("PW", userPW);
                                mBundle2.putString("Age", age + "");
                                checkmdfFragment.setArguments(mBundle2);
                                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, checkmdfFragment).commit();
                            } else {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                MemberRequest ModifyRequest = new MemberRequest(2, userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                queue.add(ModifyRequest);
            }
        });
        transbooking_tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ch_page = false;
                mDrawerLayout.closeDrawers();
                linear2.setBackgroundColor(context.getResources().getColor(R.color.touched_btn));
                setSideitem(linear1, linear3, linear1, linear4);
                replaceFragment(confirmFragment);
                titleview.setText("병원 예약 확인");
                setbitem();
            }
        });
        transfeedback_tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ch_page = false;
                mDrawerLayout.closeDrawers();
                linear3.setBackgroundColor(context.getResources().getColor(R.color.touched_btn));
                setSideitem(linear1, linear2, linear1, linear4);
                replaceFragment(fbackFragment);
                titleview.setText("피드백 모아보기");
                setbitem();
            }
        });
        condoc_tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleview.setText("담당의사 연결");
                mDrawerLayout.closeDrawers();
                setbitem();
                ch_page = false;
                linear4.setBackgroundColor(context.getResources().getColor(R.color.touched_btn));
                setSideitem(linear2, linear3, linear1, linear2);
                Bundle mBundle = new Bundle();
                mBundle.putString("userID", userID);
                mBundle.putString("userPW", userPW);
                mBundle.putString("userDoc", conDocID);
                checkpwCdFragment.setArguments(mBundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, checkpwCdFragment).commit();
            }
        });

        logout_tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                ad.setMessage("정말 로그아웃 하시겠습니까?");
                ad.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = auto.edit();
                        editor.clear();
                        editor.commit();

                        Toast toast = Toast.makeText(getApplicationContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT);
                        toast.show();
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                });
                ad.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                ad.show();

            }
        });


        //채팅

        if (ChatService.serviceIntent == null) {
            chat_intent = new Intent(getApplicationContext(), ChatService.class);
            chat_intent.putExtra("userID", userID);
            chat_intent.putExtra("docName", docName);
            chat_intent.putExtra("conDoc", conDocID);
            check_msg = true;
            startService(chat_intent);
            bindService(chat_intent, mConnection, Context.BIND_AUTO_CREATE);
        }


        //음성인식
        /*
        if (Recognition.serviceIntent==null) {
            recog_intent = new Intent(getApplicationContext(), Recognition.class);
            recog_intent.putExtra("userID",userID);
            recog_intent.putExtra("docID",conDocID);
            recog_intent.putExtra("docName",docName);
            startService(recog_intent);
        } else {
            recog_intent = Recognition.serviceIntent;//getInstance().getApplication();
            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_LONG).show();
        }
*/


        msgcnt_tf = (TextView) findViewById(R.id.cntmsg);
        Handler mHandler = new Handler(Looper.getMainLooper());


        if (BluetoothService.serviceIntent == null) {
            bluetooth_intent = new Intent(this, BluetoothService.class);
            bluetooth_intent.putExtra("userID", userID);
            bluetooth_intent.putExtra("Device", "null_device");
            bluetooth_intent.putExtra("userName", userName);
            bluetooth_intent.putExtra("docID", conDocID);
            startService(bluetooth_intent);
            bindService(bluetooth_intent, bluetoothConnection, Context.BIND_AUTO_CREATE);
            Toast.makeText(getApplicationContext(), "start bluetooth", Toast.LENGTH_LONG).show();
        }


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //채팅

                if (check_msg == true) {
                    chatDataArrayList = mService.returnChatList();
                } else {
                    chatDataArrayList = ChatService.returnChatList(); //재실행시 여기서 계속팅겨버림
                }
                array_length = chatDataArrayList.size();
                chatThread = new ChatThread();
                chatThread.start();


                //음성인식
/*
                if (Recognition.serviceIntent==null) {
                    recog_intent = new Intent(MainActivity.this, Recognition.class);
                    recog_intent.putExtra("userID",userID);
                    recog_intent.putExtra("docName",docName);
                    startService(recog_intent);
                } else {
                    recog_intent = Recognition.serviceIntent;//getInstance().getApplication();
                    Toast.makeText(getApplicationContext(), "already recog", Toast.LENGTH_LONG).show();
                }
*/
                //프로그레스다이얼로그 제거
/*
                if (Recognition.serviceIntent==null) {
                    recog_intent = new Intent(getApplicationContext(), Recognition.class);
                    recog_intent.putExtra("userID",userID);
                    recog_intent.putExtra("docID",conDocID);
                    recog_intent.putExtra("docName",docName);
                    //startService(recog_intent);
                }
*/
                //블루투스


                items = bluetoothService.getItems();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("장치 선택");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        bluetoothService.connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

//가라음성
                SttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
                SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");//한국어 사용
                mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
                //mRecognizer.setRecognitionListener(listener);

                tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != android.speech.tts.TextToSpeech.ERROR) {
                            tts.setLanguage(Locale.KOREAN);
                        }
                    }
                });

                customProgressDialog.dismiss();
            }
        }, 3000);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);


            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRecognizer.startListening(SttIntent);
                    //startService(recog_intent);
                }


            });


        }
    }
        protected void onDestroy() {
        super.onDestroy();
        if(recog_intent != null){
            stopService(recog_intent);
            recog_intent = null;
        }
        if(chat_intent != null){
            stopService(chat_intent);
            chat_intent = null;
        }
        if(bluetooth_intent != null){
            stopService(bluetooth_intent);
            bluetooth_intent = null;
        }
    }



    public class ChatThread extends Thread{
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                chatDataArrayList = mService.returnChatList();
                int compare = chatDataArrayList.size();
                msgBundle = new Bundle();
                msgBundle.putString("userID",userID);
                msgBundle.putString("conDoc",conDocID);
                msgBundle.putString("docName",docName);
                diff = compare - array_length;


                if (diff == 0) { //ㅅㅐ로운 메세지 없으면 그냥 번들로 정보 넘김
                    Log.v("채팅","compare : " + compare);
                    msgBundle.putBoolean("Check",false);
                    frag_chat.setArguments(msgBundle);
                }
                else {
                    Log.v("채팅","수신된 메세지 갯수 : "+diff);
                    if(ch_page == true){ //페이지가 넘어갈 때
                        diff = 0; //디프를 0으로 초기화하고  값 넘김
                        array_length = compare;
                        msgBundle.putString("Msg",chatDataArrayList.get(compare-1).getMsg());
                        msgBundle.putString("Date",chatDataArrayList.get(compare-1).getDate());
                        msgBundle.putString("Sender",chatDataArrayList.get(compare-1).getSender());
                        msgBundle.putString("Receiver",chatDataArrayList.get(compare-1).getReceiver());

                        if(!(chatDataArrayList.get(compare-1).getSender().equals(userID))){
                            msgBundle.putBoolean("Check",true);
                        }
                        else{ msgBundle.putBoolean("Check",false);}
                        frag_chat.setArguments(msgBundle);

                    }
                    else{
                        if(checkreceiver == false){ //받는사람이 의사면(내가 아니면)
                            Message message = handler_V.obtainMessage();
                            handler_V.sendMessage(message);
                        }
                        else{
                            diff = 0;
                        }
                    }
                }
            }
        }
    }


    public void setbitem() {
        if (bottomitem != null)
            bottomitem.setCheckable(false);
    }

    public void setSideitem(LinearLayout l1, LinearLayout l2, LinearLayout l3, LinearLayout l4) {
        l1.setBackgroundColor(context.getResources().getColor(R.color.nomal_btn));
        l2.setBackgroundColor(context.getResources().getColor(R.color.nomal_btn));
        l3.setBackgroundColor(context.getResources().getColor(R.color.nomal_btn));
        l4.setBackgroundColor(context.getResources().getColor(R.color.nomal_btn));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

     @RequiresApi(api = Build.VERSION_CODES.N)
     void replaceFragment(Fragment fragment) {
        mBundle = new Bundle();

        mBundle.putString("userID", userID);
         mBundle.putString("Name", username);
         if(! conDocID.equals("")){
             mBundle.putString("conDoc",conDocID);
         }

         fragment.setArguments(mBundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_layout, fragment).commit();      // Fragment로 사용할 MainActivity내의 layout공간을 선택합니다.
    }

    public void deliverBundle() {
        mBundle = new Bundle();
        mBundle.putString("Name", username);
        mBundle.putString("ID", userID);
        mBundle.putString("PW", userPW);
        mBundle.putString("conDoc",doc);
        mBundle.putString("Birth", userbirth);
        mBundle.putString("Gender", userGender);
        mBundle.putString("Address1", useraddress1);
        mBundle.putString("Address2", useraddress2);
        mBundle.putString("Phone", userphone);
        mBundle.putString("Email", useremail);
        mBundle.putString("Blood", userblood);
        mBundle.putString("Op", userop);
        mBundle.putString("Fd", userfd);
        mBundle.putString("Tm", usertm);
        mBundle.putString("Age", age);
        modifyFragment.setArguments(mBundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, modifyFragment).commit();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public int calAge(String birthday) {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date ymd = new Date();
        String today = format1.format(ymd);
        int age = 0;
        String str[] = birthday.split("/");
        String str2[] = today.split("-");

        int year = Integer.parseInt(str[0]);
        int month = Integer.parseInt(str[1]);
        int day = Integer.parseInt(str[2]);
        int to_year = Integer.parseInt(str2[0]);
        int to_month = Integer.parseInt(str2[1]);
        int to_day = Integer.parseInt(str2[2]);

        if (to_year - year == 0) {
            age = 1;
        } else {
            if (month > to_month && (month == to_month && (day == to_day || to_day > day))) {
                age = to_year - year;
            } else {
                age = to_year - year - 1;
            }
        }
        return age;
    }

    public void setRecentlyRes(Bundle mBundle, String r_time, String r_date, String
            d_name, String d_work) {
        mBundle.putString("r_time", r_time);
        mBundle.putString("r_date", r_date);
        mBundle.putString("d_name", d_name);
        mBundle.putString("d_work", d_work);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // 기존 뒤로 가기 버튼의 기능을 막기 위해 주석 처리 또는 삭제

        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지났으면 Toast 출력
        // 2500 milliseconds = 2.5 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2500) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "뒤로 가기 버튼을 한 번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간에 2.5초를 더해 현재 시간과 비교 후
        // 마지막으로 뒤로 가기 버튼을 눌렀던 시간이 2.5초가 지나지 않았으면 종료
        if (System.currentTimeMillis() <= backKeyPressedTime + 2500) {

            toast.cancel();
            toast = Toast.makeText(this, "이용해 주셔서 감사합니다.", Toast.LENGTH_LONG);
            toast.show();
            ActivityCompat.finishAffinity(this);
        }
    }

    public static void toast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(MReceiver, new IntentFilter("com.example.action.recognition.trans"));
        LocalBroadcastManager.getInstance(this).registerReceiver(MReceiver, new IntentFilter("com.example.action.chatservice"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("abc"));
        LocalBroadcastManager.getInstance(this).registerReceiver(muscleAlarmReceiver, new IntentFilter("com.example.action.bluetoothservice.alarm"));
        LocalBroadcastManager.getInstance(this).registerReceiver(callReceiver, new IntentFilter("tell"));
    }
    protected void onPause(){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(MReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(CReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(muscleAlarmReceiver);
    }
    private BroadcastReceiver callReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("check").equals("true")) {
                String tel = "tel:119";
                Intent showIntent = new Intent(getApplicationContext(), MainActivity.class);
                showIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                showIntent.putExtra("android.intent.action.DIAL", Uri.parse(tel));
                System.out.println(tel);
                startActivity(new Intent("android.intent.action.DIAL", Uri.parse(tel)));
            }
        }

    };
    private BroadcastReceiver muscleAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("alarm_request").equals("vibrate")) {
                System.out.println("자세가 불균형합니다. 바른 자세를 유지해 주세요.");
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(1000); //1초간 진동

            }
        }
    };
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            replace_confirm();
        }
    };
    private BroadcastReceiver MReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("recog_trans");

            if("muscle".equals(value)){
                replaceFragment(muscleFragment);
                titleview.setText("근전도 기준 측정");
                setSideitem(linear1, linear2, linear3,linear4);

            }
            else if("data".equals(value)){
                replaceFragment(frag_data);
                titleview.setText("건강 데이터");
                setSideitem(linear1, linear2, linear3,linear4);
            }
            else{

            }

        }
    };
    private BroadcastReceiver CReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String value = intent.getStringExtra("alarm_request");
            setAlarm(value);

        }
    };


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void replace_confirm(){
        mDrawerLayout.closeDrawers();
        linear2.setBackgroundColor(context.getResources().getColor(R.color.touched_btn));
        setSideitem(linear1, linear3, linear1, linear3);
        replaceFragment(confirmFragment);
        titleview.setText("병원 예약 확인");
        setbitem();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setAlarm(String date) {
        //AlarmReceiver에 값 전달
        Intent receiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, receiverIntent, 0);
        String from = date; //임의로 날짜와 시간을 지정
        //날짜 포맷을 바꿔주는 소스코드
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date datetime = null;
        try {
            datetime = dateFormat.parse(from);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datetime);

        if(calendar.getTimeInMillis() > System.currentTimeMillis()) {   //설정시간이 현재보다 미래일 경우
            alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        }
    }







    public Frag_main get_frag_main() {
        return frag_main;
    }
    private void setUpBot() {
        try {
            InputStream stream = this.getResources().openRawResource(R.raw.credential);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream).
                    createScoped("https://www.googleapis.com/auth/cloud-platform");
            String projectid = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.
                    create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectid, uuid);
            Log.d(TAG, "projectid : " + projectid);
        } catch (IOException e) {
            Log.d(TAG, "setUpBot : " + e.getMessage());
        }
    }

    //dialogflow로 message를 보내는 메서드
    private void sendMessageToBot(String message) {
        rsp = null;
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("ko-KR")).build();
        new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
    }

    public void callback(DetectIntentResponse returnResponse) {
        //dialogflowAgent와 통신 성공한 경우
        if (returnResponse != null) {
            String dialogflowBotReply = returnResponse.getQueryResult().getFulfillmentText();
            Log.d("text", returnResponse.getQueryResult().toString());
            //getFulfillmentText가 있을 경우
            if (!dialogflowBotReply.isEmpty()) {
                rsp = dialogflowBotReply;
                System.out.println("응답입니다. : " + dialogflowBotReply);
            } else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
        }
    }
}



