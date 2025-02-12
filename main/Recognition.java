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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import com.example.main.Request.RecognitionRequest;
import com.example.main.interfaces.DialogflowBotReply;
import com.example.main.utils.SendMessageInBg;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import io.opencensus.metrics.LongGauge;

import static android.speech.tts.TextToSpeech.ERROR;

public class Recognition extends RecognitionService implements DialogflowBotReply {

    public static final int MSG_VOICE_RECO_READY = 0;
    public static final int MSG_VOICE_RECO_END = 1;
    public static final int MSG_VOICE_RECO_RESTART = 2;
    boolean mBoolVoiceRecoStarted;

    int rec_case = 0, check = 0;
    boolean timelist[] = new boolean[8];

    private Intent mIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private TextToSpeech tts;
    SpeechRecognizer mRecognizer;
    private String msg, rsp;
    private Handler mHandler = new Handler(Looper.getMainLooper()); //tts를 듣는 시간 벌기 위한 타임딜레이
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private String uuid = UUID.randomUUID().toString();
    private static String TAG = "tag";
    private String userID = "";
    private RecognitionRequest recognitionRequest;
    private String date;
    private String receiver = "", docName = "";
    private RequestQueue queue;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static int mode = 0;
    // 0 : 측정아닌상태, 1 : 일반모드, 2 : 수면모드, 3 : 운동모드
    public static Intent serviceIntent = null;
    private AES256Chiper aes256Chiper;
    private boolean checkreceiver = false;
    private Frag_main frag_main;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate() {
        super.onCreate();
        queue = Volley.newRequestQueue(getApplicationContext());
        aes256Chiper = new AES256Chiper();
        frag_main = new Frag_main();
        setUpBot();
        startListening();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;
        userID = intent.getStringExtra("userID");
        receiver = intent.getStringExtra("docID");
        docName = intent.getStringExtra("docName");

        return super.onStartCommand(intent, flags, startId);
    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, ServiceAlarmReceiver.class);
        intent.putExtra("userID",userID);
        intent.putExtra("docName", docName);
        intent.putExtra("docID",receiver);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0,intent,0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(tts != null) {

            tts.stop();
            tts.shutdown();
            Log.d(TAG, "TTS Destroyed");
        }
        serviceIntent = null;
        //setAlarmTimer();

    }
    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {

    }

    private Handler mHdrVoiceRecoState = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_VOICE_RECO_READY:
                    break;
                case MSG_VOICE_RECO_END: {
                    stopListening();
                    sendEmptyMessageDelayed(MSG_VOICE_RECO_RESTART, 1500);
                    break;
                }
                case MSG_VOICE_RECO_RESTART:
                    startListening();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    public void startListening(){  //음성인식 시작(준비)
        Log.v("음성인식","스타트리스닝");
        Toast.makeText(getApplicationContext(),"startListening",Toast.LENGTH_LONG);
        mIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        if (tts == null){
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status != ERROR) {
                        tts.setLanguage(Locale.KOREAN);
                        System.out.println("랄랄라랄라라랄라라라라라라라라랄");
                    }
                }
            });
            Log.v("음성인식","tts설정");
        }

        mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
            @Override
            public void run() {

                if (mBoolVoiceRecoStarted == false) // 아직 인식 준비 안된 상태라면,
                {
                    if (mRecognizer == null) //스피치 레커그나이저가 없다면,
                    {
                        mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                        mRecognizer.setRecognitionListener(listener);
                    }
                    if (mRecognizer.isRecognitionAvailable(getApplicationContext())) //레커그나이저가 사용 가능한 상태라면(만들어 졌다면)
                    {
                        //인식할 준비(레커그나이저 세팅)
                        mIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
                        mIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, (getApplication()).getPackageName());
                        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                        mIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 50);

                        mRecognizer.startListening(mIntent);


                    }
                }

                mBoolVoiceRecoStarted = true; // 음성인식 시작된 상태

            }
        }, 3000);

    }

    public void stopListening() //음성인식 멈춤
    {
        try {
            if (mRecognizer != null && mBoolVoiceRecoStarted == true) // 스피치레커그나이저 생성되었고, 인식 시작된 상태라면,
            {
                mRecognizer.stopListening(); // 인식 멈춤
                mRecognizer.destroy();
                mRecognizer = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        mBoolVoiceRecoStarted = false; // 음성인식 시작된 상태 아님 표시
    }

    @Override
    protected void onCancel(Callback listener) {
    }

    @Override
    protected void onStopListening(Callback listener) {
    }

    private RecognitionListener listener = new RecognitionListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReadyForSpeech(Bundle params) {
            //Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
            Log.v("음성인식","6");

        }

        @Override
        public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
            //Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. 1 : " + message, Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 3000);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onResults(Bundle results) { // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            msg = matches.get(0);
            System.out.println("msg : " + msg);


            if ("헬스케어".equals(msg)) {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
                readText("네 무엇을 도와드릴까요?");
                mHandler.postDelayed(new Runnable() { //tts듣기위한 딜레이
                    @Override
                    public void run() {
                        mRecognizer.setRecognitionListener(listener1); // 실제 수행할 명령 입력 및 인식
                        mRecognizer.startListening(mIntent);
                    }
                }, 3000);
            } else {
                mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                    @Override
                    public void run() {
                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                    }
                }, 3000);
            }


        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    private RecognitionListener listener1 = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "말씀해주세요", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
            MainActivity.checkreceiver = false;
        }

        @Override
        public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다.2 : " + message, Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 3000);
        }

        @Override
        public void onResults(Bundle results) { // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.

            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            msg = matches.get(0); //새로운 명령문 인식

            System.out.println("메세지 : " + msg);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            sendMessageToBot(msg);//다이얼로그 플로우에 저장


            mHandler.postDelayed(new Runnable() {//타임딜레이
                public void run() {
                    System.out.println("392rsp"+rsp);
                    Log.d("123Rsp",rsp);
                    if ("아니".equals(rsp) || "글쎄".equals(rsp)) {
                        readText("필요한 일이 생기면 다시 불러주세요.");
                        rec_case = 0;
                    } else if (rsp == null || rsp =="" || rsp =="null") { //디폴트 값 (안녕) 말했을 때
                        readText("제가 이해할 수 있는 말이 아니에요.");
                        rec_case = 0;
                    } else {
                        String object = rsp.split("/")[0]; // 병원
                        String action = rsp.split("/")[1]; // 예약해줘
                        System.out.println("obj : " + object);
                        System.out.println("act : " + action);

                        if ("예약".equals(object)) { //병원 예약내역 확인
                            if ("확인".equals(action) || "읽".equals(action)) {
                                // 화면전환
                                readText("전체 내역을 읽어드릴까요?");
                                rec_case = 2;

                            } else { //액션이 널일때도 걸린다
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("병원".equals(object)) {
                            if ("예약".equals(action)) {
                                int check = 0;
                                readText("원하는 날짜를 말씀해 주세요");
                                rec_case = 1;

                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("피드백".equals(object)) {
                            if ("확인".equals(action) || "읽".equals(action)) {
                                readText("확인을 원하는 날짜를 말씀해 주세요");
                                rec_case = 3;
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("메세지".equals(object)) {
                            if ("보내".equals(action)) {
                                readText("전송할 메세지 내용을 말해주세요");
                                rec_case = 4;
                            } else if ("읽".equals(action) || "확인".equals(action)) {
                                readText("가장 최근에 수신한 메세지를 읽어드릴까요?");
                                rec_case = 5;
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("운동모드".equals(object)) {
                            if ("측정".equals(action)) {
                                //BluetoothService.mode = 3;
                                rec_case = 0;
                            } else if ("종료".equals(action)) {
                                //BluetoothService.mode = 1;
                                rec_case = 0;
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("수면모드".equals(object)) {
                            if ("측정".equals(action)) {
                                // BluetoothService.mode = 2;
                                rec_case = 0;
                            } else if ("종료".equals(action)) {
                                //BluetoothService.mode = 1;
                                rec_case = 0;
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("일반모드".equals(object)) {
                            if ("측정".equals(action)) {
                                //BluetoothService.mode = 1;
                                rec_case = 0;
                            } else if ("종료".equals(action)) {
                                // BluetoothService.mode = 0;
                                rec_case = 0;
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("근전도 기준".equals(object)) {
                            if ("측정".equals(action)) {
                                replace_muscle();
                                rec_case = 0; // 어차피 페이지 넘어가고 음성인식 새로 시작이고 근전도는 별도측정
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        } else if ("데이터".equals(object)) {
                            if ("확인".equals(action)) {
                                replace_confirm();
                                rec_case = 0;
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                rec_case = 0;
                            }
                        }
                        else{
                            readText("무슨 말씀이신지 잘 모르겠어요.");
                            rec_case = 0;
                        }
                    }

                    switch (rec_case) {
                        case 0:
                            System.out.println("rec_case = 0");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                                }
                            },3000);
                            break;
                        case 1: //날짜 입력
                            System.out.println("rec_case = 1");

                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    check = 0;
                                    mRecognizer.setRecognitionListener(listener2); // 실제 수행할 명령 입력 및 인식
                                    mRecognizer.startListening(mIntent);
                                }
                            }, 3000);
                            break;
                        case 2:
                            System.out.println("rec_case = 2");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    check = 0;
                                    mRecognizer.setRecognitionListener(listener3); // 예약읽어주는 리스너
                                    mRecognizer.startListening(mIntent);
                                }
                            }, 3000);
                            break;
                        case 3:
                            System.out.println("rec_case = 3");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    check = 0;
                                    mRecognizer.setRecognitionListener(listener5); // 예약읽어주는 리스너
                                    mRecognizer.startListening(mIntent);
                                }
                            }, 3000);
                            break;
                        case 4: //ok
                            System.out.println("rec_case = 4");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    check = 0;
                                    MainActivity.checkreceiver = true;
                                    mRecognizer.setRecognitionListener(listener7); // 메세지 전송 리스너
                                    mRecognizer.startListening(mIntent);
                                }
                            }, 3000);
                            break;
                        case 5:
                            System.out.println("rec_case = 5");
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    check = 0;
                                    mRecognizer.setRecognitionListener(listener8); // 최근 수신 메세지 읽는 리스너
                                    mRecognizer.startListening(mIntent);
                                }
                            }, 3000);
                            break;
                    }

                }
            }, 3000);


        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

    };
    private RecognitionListener listener3 = new RecognitionListener() { //전체 예약내역 읽어주는 리스너
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다 : " + error, Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 3000);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
            String msg = result.get(0);

            sendMessageToBot(msg);//다이얼로그 플로우에 전송

            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    if ("응".equals(rsp) || "읽".equals(rsp)) {
                        try {
                            readMyRes(); //함수호출 필요없으면 그냥바로 여기에 쓸거임
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
                    } else if ("아니".contains(rsp) || "글쎄".contains(rsp)) {
                        readText("필요한 일이 생기면 다시 불러주세요.");
                    } else {
                        readText("무슨 말씀이신지 잘 모르겠어요.");
                    }

                }
            }, 3000);


        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private RecognitionListener listener2 = new RecognitionListener() { //특정날짜 입력받아오기(병원예약)
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다 : " + error, Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 3000);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResults(Bundle results) { //병원예약날짜받아오기
            ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
            String msg = result.get(0);
            System.out.println("리스너2 : " + msg);

            if (msg.contains("월") && msg.contains("일")) {
                int m = Integer.parseInt(msg.split("월")[0]);
                int d = Integer.parseInt(msg.split("월")[1].replace(" ", "").split("일")[0]);
                if(checkDay_booking(m,d)){
                    System.out.println(m + "월" + d + "일");
                    Calendar cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    if(cal.get(Calendar.MONTH) == 12 && m == 1){
                        year++;
                    }
                    date = year+ "/" + m + "/" + d;
                    Toast.makeText(Recognition.this, date, Toast.LENGTH_SHORT).show();
                    Response.Listener<String> responseRes = new Response.Listener<String>() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                int i = 0;
                                Arrays.fill(timelist, true);
                                while (i < jsonArray.length()) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    boolean success = jsonObject.getBoolean("success");
                                    String time = aes256Chiper.decoding(jsonObject.getString("resTime"),MainActivity.key);
                                    System.out.println("시간 : " + time);
                                    if (success) {
                                        switch (time) {
                                            case "10:30":
                                                timelist[0] = false;
                                                break;
                                            case "11:00":
                                                timelist[1] = false;
                                                break;
                                            case "13:30":
                                                timelist[2] = false;
                                                break;
                                            case "14:00":
                                                timelist[3] = false;
                                                break;
                                            case "14:30":
                                                timelist[4] = false;
                                                break;
                                            case "15:00":
                                                timelist[5] = false;
                                                break;
                                            case "15:30":
                                                timelist[6] = false;
                                                break;
                                            case "16:00":
                                                timelist[7] = false;
                                                break;
                                            default:
                                                break;
                                        }
                                    } else {

                                    }
                                    i++;
                                }
                                readText("예약가능한 시간은 ");
                                for (int j = 0; j < 8; j++) {
                                    if (timelist[j] == true) {
                                        findTime(j);
                                        SystemClock.sleep(1500);
                                    }
                                }
                                readText("입니다. 원하는 예약 시간을 말씀해 주세요.");
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        check = 0;
                                        mRecognizer.setRecognitionListener(listener4); // 실제 수행할 명령 입력 및 인식
                                        mRecognizer.startListening(mIntent);
                                    }
                                }, 3000);
                            } catch (JSONException e) {
                                e.printStackTrace();
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
                    try {
                        String e_id = aes256Chiper.encoding(userID, MainActivity.key);
                        String e_date = aes256Chiper.encoding(date, MainActivity.key);
                        recognitionRequest = new RecognitionRequest(e_id, e_date, responseRes);
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        queue.add(recognitionRequest);
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

                }
                else{
                    if (check < 2) {
                        readText("예약이 불가능 한 날짜입니다. 다른 날짜를 말해주세요.");
                        check++;
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                mRecognizer.setRecognitionListener(listener2); // 실제 수행할 명령 입력 및 인식
                                mRecognizer.startListening(mIntent);
                            }
                        }, 3000);
                    } else {
                        readText("무슨 말씀이신지 잘 모르겠어요.");
                        check = 0;
                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);

                    }
                }



            } else {
                if (check < 2) {
                    readText("특정 월과 일을 정확히 말해주세요.");
                    check++;
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            mRecognizer.setRecognitionListener(listener2); // 실제 수행할 명령 입력 및 인식
                            mRecognizer.startListening(mIntent);
                        }
                    }, 3000);
                } else {
                    readText("무슨 말씀이신지 잘 모르겠어요.");
                    check = 0;
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);

                }

            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private RecognitionListener listener4 = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
            String msg = result.get(0);
            System.out.println("리스너4 : " + msg);
            int h = 0, m = 0;
            String mm = "00";

            if (msg.contains("시")) {
                h = Integer.parseInt(msg.split("시")[0]);
                if (h < 10) {
                    h += 12;
                }

                if (msg.contains("분")) {
                    m = Integer.parseInt(msg.split("시")[1].replaceAll(" ", "").split("분")[0]);
                    mm = Integer.toString(m);
                } else if (msg.contains("반")) {
                    m = 30;
                    mm = Integer.toString(m);
                }

                System.out.println(h + "시 " + mm + "분");
                Toast.makeText(getApplicationContext(),"시간 : "+ h + "시 " + mm + "분" , Toast.LENGTH_SHORT).show();
                String time = h + ":" + mm;
                boolean ch = false;
                try {
                    ch = bookTime(time, date); //여기서 시간 지정에 관한 조건은 다 거름
                    if (ch == false) {
                        if (check < 2) {
                            readText("예약할 수 없는 시간입니다. 다른 시간대를 선택해주세요.");
                            check++;
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    mRecognizer.setRecognitionListener(listener4); // 실제 수행할 명령 입력 및 인식
                                    mRecognizer.startListening(mIntent);
                                }
                            }, 3000);
                        } else {
                            readText("무슨 말씀이신지 잘 모르겠어요.");
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    check = 0;
                                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                                }
                            }, 3000);
                        }
                    } else {
                        readText("해당 날짜에 예약이 완료되었습니다.");
                        Toast.makeText(getApplicationContext(),"예약완료" , Toast.LENGTH_SHORT).show();
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                check = 0;
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                            }
                        }, 3000);
                    }
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
                System.out.println("ch : " + ch);

            } else {
                readText("무슨 말씀이신지 잘 모르겠어요.");
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        check = 0;
                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                    }
                }, 3000);

            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private RecognitionListener listener5 = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다 : " + error, Toast.LENGTH_SHORT).show();

            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 3000);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
            String msg = result.get(0);
            System.out.println("리스너2 : " + msg);
            Toast.makeText(getApplicationContext(), "피드백 인식", Toast.LENGTH_SHORT).show();

            sendMessageToBot(msg);

            mHandler.postDelayed(new Runnable() {
                Handler mHandler2 = new Handler();
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    if (rsp.equals("글쎄")) {
                        readText("가장 최근 피드백을 읽어드릴까요?");
                        mHandler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mRecognizer.setRecognitionListener(listener6); // 실제 수행할 명령 입력 및 인식
                                mRecognizer.startListening(mIntent);
                            }
                        }, 3000);
                    } else if (rsp.equals("아니")) {
                        readText("필요한 일이 생기면 다시 불러주세요");
                        mHandler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1

                            }
                        }, 3000);
                    } else if (rsp.equals("날짜")) { // 월만 들어가거나 일만 들어가도 날짜에 걸림
                        if (msg.contains("년") && msg.contains("월") && msg.contains("일")) {//그래서 여기 이프로 걸러준거 (두개 다 들어가야.)
                            String message = msg.replaceAll(" ","");
                            int y = Integer.parseInt(message.split("년")[0]);
                            int m = Integer.parseInt(message.split("년")[1].split("월")[0]);
                            int d = Integer.parseInt(message.split("년")[1].split("월")[1].split("일")[0]);
                            Toast.makeText(getApplicationContext(), "피드백메세지날짜 : "+message, Toast.LENGTH_SHORT).show();
                            String mm = "";
                            if(checkDay_feedb(y,m,d)){
                                System.out.println(m + "월" + d + "일");
                                if (m < 10) {
                                    mm = "0" + m;
                                }
                                date = y + "/" + mm + "/" + d;
                                Toast.makeText(getApplicationContext(), "피드백메세지날짜 : "+date, Toast.LENGTH_SHORT).show();
                                try {
                                    readFb(date);
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
                            }
                            else{
                                if (check < 2) {
                                    readText("선택할 수 없는 날짜입니다. 날짜를 다시 선택해주세요.");
                                    check++;
                                    mHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            mRecognizer.setRecognitionListener(listener5); // 실제 수행할 명령 입력 및 인식
                                            mRecognizer.startListening(mIntent);
                                        }
                                    }, 3000);
                                } else {
                                    readText("무슨 말씀이신지 잘 모르겠어요.");
                                    check = 0;
                                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                                }
                            }

                        } else {
                            if (check < 2) {
                                readText("특정 월과 일을 정확히 말해주세요.");
                                check++;
                                mHandler.postDelayed(new Runnable() {
                                    public void run() {
                                        mRecognizer.setRecognitionListener(listener5); // 실제 수행할 명령 입력 및 인식
                                        mRecognizer.startListening(mIntent);
                                    }
                                }, 3000);
                            } else {
                                readText("무슨 말씀이신지 잘 모르겠어요.");
                                check = 0;
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                            }
                        }
                    }
                    else {
                        readText("무슨 말씀이신지 잘 모르겠어요.");
                        check = 0;
                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END);
                    }
                }
            }, 3000);
        }


        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private RecognitionListener listener6 = new RecognitionListener() { //최근피드백읽기
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다 : " + error, Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 3000);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
            String msg = result.get(0);
            System.out.println("리스너6 : " + msg);
            sendMessageToBot(msg);
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {
                    if ("응".equals(rsp) || "읽".equals(rsp)) {
                        try {
                            readFb("no");
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
                    } else { //아니, 글쎄
                        readText("필요한 일이 생기면 다시 불러주세요.");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                            }
                        }, 3000);
                    }
                }
            }, 3000);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private RecognitionListener listener7 = new RecognitionListener() { //메세지 전송 리스너
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다 : " + error, Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 1000);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            msg = matches.get(0); //새로운 명령문 인식

            Date d = new Date();
            String _date = format.format(d);

            Response.Listener<String> responseListener_send = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            readText("메세지가 전송되었습니다.");
                            MainActivity.chatDataArrayList.add(new ChatData(msg, _date, userID, receiver));
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                                }
                            }, 3000);


                        } else {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };

            try {
                String e_msg = aes256Chiper.encoding(msg, MainActivity.key);
                String e_date = aes256Chiper.encoding(_date, MainActivity.key);
                String e_id = aes256Chiper.encoding(userID, MainActivity.key);
                String e_receiver = aes256Chiper.encoding(receiver, MainActivity.key);
                String e_send = aes256Chiper.encoding("1",MainActivity.key);
                String e_chk = aes256Chiper.encoding("1",MainActivity.key);
                RecognitionRequest recognitionRequest = new RecognitionRequest(2, e_msg, e_date, e_id, e_receiver, e_send,e_chk,responseListener_send);
                queue.add(recognitionRequest);
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


        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private RecognitionListener listener8 = new RecognitionListener() { //메세지 읽기
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다 : " + error, Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() { //tts 듣기위한, 토스트 보기위한 시간 딜레이
                @Override
                public void run() {
                    mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                }
            }, 3000);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            msg = matches.get(0); //새로운 명령문 인식
            sendMessageToBot(msg);


            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void run() {

                    if ("응".equals(rsp) || "읽".equals(rsp)) {
                        Response.Listener<String> responseListener2 = new Response.Listener<String>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    boolean success = jsonObject.getBoolean("success");
                                    if (success) {
                                        String msg = jsonObject.getString("msg");
                                        String date = jsonObject.getString("date");

                                        String e_msg = aes256Chiper.decoding(msg, MainActivity.key);
                                        String e_date = aes256Chiper.decoding(date, MainActivity.key);

                                        String[] tmp_d = e_date.split(" ")[0].split("/");
                                        String[] tmp_t = e_date.split(" ")[1].split(":");

                                        String re_date = tmp_d[0] + "년 " + tmp_d[1] + " 월" + tmp_d[2] + " 일";
                                        String re_time = tmp_t[0] + "시 ";
                                        if (!"00".contains(tmp_t[1])) {
                                            re_time = re_time + tmp_t[1] + "분";
                                        }

                                        readText(re_date + " " + re_time + "에 수신된 메세지입니다.");
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                readText(e_msg);
                                                Handler mHandler2 = new Handler();
                                                mHandler2.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                                                    }
                                                }, 3000);
                                            }
                                        }, 3000);
                                    }
                                    else{
                                        readText("수신된 메세지가 없습니다.");
                                        mHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                                            }
                                        }, 3000);

                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        try {
                            String e_id = aes256Chiper.encoding(userID, MainActivity.key);
                            recognitionRequest = new RecognitionRequest(3, 3, e_id, responseListener2);
                            queue.add(recognitionRequest);
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

                    } else {
                        readText("필요한 일이 생기면 다시 불러주세요.");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                            }
                        }, 3000);
                    }
                }
            }, 3000);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    private void replace_muscle() {
        Intent intent = new Intent("com.example.action.recognition.trans");
        intent.putExtra("recog_trans", "muscle");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void  replace_confirm() {
        Intent intent = new Intent("com.example.action.recognition.trans");
        intent.putExtra("recog_trans", "data");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void readFb(String d) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "피드백 디비212121", Toast.LENGTH_SHORT).show();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    Toast.makeText(Recognition.this, "피드백 리스너 동작함", Toast.LENGTH_SHORT).show();
                    if (success) {

                        String feedback = aes256Chiper.decoding(jsonObject.getString("fContent"),MainActivity.key);
                        String symptom = aes256Chiper.decoding(jsonObject.getString("fSymptom"),MainActivity.key);
                        String doctor = aes256Chiper.decoding(jsonObject.getString("docName"),MainActivity.key);

                        Toast.makeText(Recognition.this, "피드백 리스너 :"+symptom, Toast.LENGTH_SHORT).show();


                        readText(doctor + " 선생님이 " + symptom + " 증상에 대해 남기신 피드백입니다.");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                readText(" " + feedback);
                                Handler mHandler2 = new Handler();
                                mHandler2.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                                    }
                                }, 10000);
                            }
                        }, 7000);
                    } else {
                        readText("해당 날짜에는 피드백이 존재하지 않습니다.");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 1
                            }
                        }, 3000);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        String e_id = aes256Chiper.encoding(userID,MainActivity.key);
        String e_date = aes256Chiper.encoding(d, MainActivity.key);

        if (d.equals("no")) {
            recognitionRequest = new RecognitionRequest(6, e_id, responseListener); //최근피드백읽기
        } else {
            recognitionRequest = new RecognitionRequest(4, e_id, e_date, responseListener); //특정날짜피드백읽기
        }
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(recognitionRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean bookTime(String t, String d) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String time = "no";
        final boolean[] check = {true};
        switch (t) {
            case "10:30":
                if (timelist[0] == true)
                    time = "10:30";
                break;
            case "11:00":
                if (timelist[1] == true)
                    time = "11:00";
                break;
            case "13:30":
                if (timelist[2] == true)
                    time = "13:30";
                break;
            case "14:00":
                if (timelist[3] == true)
                    time = "14:00";
                break;
            case "14:30":
                if (timelist[4] == true)
                    time = "14:30";
                break;
            case "15:00":
                if (timelist[5] == true)
                    time = "15:00";
                break;
            case "15:30":
                if (timelist[6] == true)
                    time = "15:30";
                break;
            case "16:00":
                if (timelist[7] == true)
                    time = "16:00";
                break;
            default:
                break;
        }

        if (!(time.equals("no"))) {
            Response.Listener<String> responseListener = new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        Toast.makeText(getApplicationContext(), "예약 인서트성공", Toast.LENGTH_SHORT).show();
                        if (success) {
                            check[0] = true;
                        } else {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            String e_id = aes256Chiper.encoding(userID,MainActivity.key);
            String e_date = aes256Chiper.encoding(d, MainActivity.key);
            String e_time = aes256Chiper.encoding(time, MainActivity.key);
            String e_doc = aes256Chiper.encoding(receiver, MainActivity.key);
            recognitionRequest = new RecognitionRequest(e_id, e_date, e_time, e_doc, responseListener);
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(recognitionRequest);
        } else {
            check[0] = false;
        }

        return check[0];
    }
    public boolean checkDay_feedb(int y, int m, int d){
        Date date = new Date();
        SimpleDateFormat form = new SimpleDateFormat("yyyy/MM/dd");
        String day = form.format(date);
        int yy = Integer.parseInt(day.split("/")[0]);
        int mm =Integer.parseInt(day.split("/")[1]);
        int dd = Integer.parseInt(day.split("/")[2]);

        if(m > 12 || m < 1 || d < 0 || d > 31){ // 날짜의 숫자들이 올바르지 않을 때
            return false;
        }
        else if(m > mm){ //예를 들어 입력날짜는 12월인데 오늘은 1월일때
            if(yy > y){ //이게 만약 년도가 다른거라면 오케이
                return true;
            }
            else{
                return false;
            }
        }
        else if(m == mm){
            if(dd > d){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "날짜체크 :"+true, Toast.LENGTH_SHORT).show();

            return true;
        }
    }
    public boolean checkDay_booking(int m, int d){
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        SimpleDateFormat form = new SimpleDateFormat("MM/dd");
        String day = form.format(date.getTime());

        int mm = Integer.parseInt(day.split("/")[0]);
        int dd = Integer.parseInt(day.split("/")[1]);

        date.add(Calendar.DATE,31);

        String limit_day = form.format(date.getTime());
        int limit_m = Integer.parseInt(limit_day.split("/")[0]);
        int limit_d = Integer.parseInt(limit_day.split("/")[1]);


        if(m > 12 || m < 1 || d < 0 || d > 31){ // 날짜의 숫자들이 올바르지 않을 때
            return false;
        }
        else if(m < mm || (mm == m && d < dd)){// 오늘날짜보다 이전의 날짜일 때
            if(mm == 12 && m == 1){ //다음년도기 때문에 가능함
                return true;
            }
            else{
                return false;
            }
        }
        else if(m == mm && d == dd){ //당일예약일때 (당일예약 막아놨음 )
            return false;
        }
        else if(m >= limit_m ){ //오늘이후로부터 한달이후(예약가능기간이 한달앞쪽까지만임.)
            if(m == limit_m+1 && d <= limit_d){ // 근데 만약에 달이 한달이후고 날짜는 작으면, (어차피 31일 이후에 최대는 리미트 일과 현재일이 같은것 뿐)
                return true;
            }
            else{
                return false;
            }

        }
        else{
            return true;
        }
    }


    public void readText(String str) {

        System.out.println("1");
        tts.setPitch(1.0f);         // 음성 톤은 기본 설정
        System.out.println("2");
        tts.setSpeechRate(1.0f);    // 읽는 속도를 0.5빠르기로 설정
        System.out.println("3");
        // editText에 있는 문장을 읽는다.
        tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        System.out.println("4");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void readMyRes() throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        System.out.println("요청");
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if (jsonArray.length() != 0) {
                        int i = 0;
                        while (i < jsonArray.length()) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                String date = jsonObject.getString("resDate");
                                String time = jsonObject.getString("resTime");

                                String d_date = aes256Chiper.decoding(date,MainActivity.key);
                                String d_time = aes256Chiper.decoding(time, MainActivity.key);

                                String str[] = d_date.split("/");
                                String str2[] = d_time.split(":");
                                date = str[0] + "년 " + str[1] + "월 " + str[2] + "일 ";
                                time = str2[0] + "시 ";
                                if (!"00".equals(str2[1])) {
                                    time += str2[1] + "분 ";
                                }
                                readText(date + time); //몇년 몇월 며칠 몇시 몇분
                                System.out.println(date + time);
                                SystemClock.sleep(4000); //1초간격
                                System.out.println("쉼");
                                i++;

                            }
                        }
                        readText("에 병원 예약 내역이 있습니다.");
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 다시 헬스케어 부를 수 있게
                            }
                        }, 3000);
                    } else {
                        readText("병원 예약 내역이 존재하지 않습니다.");
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                mHdrVoiceRecoState.sendEmptyMessage(MSG_VOICE_RECO_END); // 다시 헬스케어 부를 수 있게
                            }
                        }, 3000);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        String e_id = aes256Chiper.encoding(userID, MainActivity.key);
        recognitionRequest = new RecognitionRequest(e_id, responseListener);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(recognitionRequest);
    }


    public void findTime(int index) {
        //포문으로 말할 때 딜레이 (물리는거 안됨)
        switch (index) {
            case 0:
                readText("열시 삼십분");
                break;
            case 1:
                readText("열한시");
                break;
            case 2:
                readText("한시 삼십분");
                break;
            case 3:
                readText("두시");
                break;
            case 4:
                readText("두시 삼십분");
                break;
            case 5:
                readText("세시");
                break;
            case 6:
                readText("세시 삼십분");
                break;
            case 7:
                readText("네시");
                break;

        }
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
