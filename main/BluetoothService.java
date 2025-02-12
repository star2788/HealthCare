package com.example.main;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.BluetoothRequest;
import com.example.main.Request.FeedbackRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class BluetoothService extends Service {

    private final IBinder mBinder = new LocalBinder();
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    private MyQueue tempQueue = new MyQueue(11);
    private MyQueue heartQueue = new MyQueue(11);
    private MyQueue muscleQueue = new MyQueue(11);


    private static int accel_x, accel_y, accel_z = 0;
    //private static int temp = 0;


    private static String msg;
    private int i = 0;
    private SimpleDateFormat dformat = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat tformat = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat tsformat = new SimpleDateFormat("HH:mm:ss");
    private Date dt;
    private String id = ""; //디비에서 가져오기 위한 사용자 아이디
    boolean notnom = false; // 직전값 정상 비저상 판단하기 위한 변수
    private int a = 0;
    private int b = 0;
    String beforet = ""; // 제일 마지막 값 삭제하기 위해
    String beforeh = "";
    String beforem = "";
    private String limit_d = "";
    private String modecheck = "";
    private String name = "한지평";
    private String docID;


    private static StringBuffer str = new StringBuffer();
    static String str1;

    double t_avg = 0.0; //값 정상 비정상 판단 위한 에버리지
    int h_avg = 0;
    int Rm_avg = 0;
    int Lm_avg = 0;

    double t_sum = 0; //전제 더한 값
    int h_sum = 0;
    int Rm_sum = 0;
    int Lm_sum = 0;

    int end = 10; // 이상값 다음으로 저장할 1초단위 값의 갯수를 늘려간다.

    private AES256Chiper aes256Chiper;

    private boolean check_t = false;
    private boolean check_h = false;
    private boolean check_m = false; // 근전도 자세 기울어짐 판단

    private static int before_x = 0;
    private static int before_y = 0;
    private static int before_z = 0;
    private static int before_svm = 0;

    public static int mode = 1;
    public static int sensor_count = 1;
    private static int ABS = 0, SVM = 0;

    public static Intent serviceIntent = null;
    private static String Device_name = "";

    String key = "qwertyuiopasdfghjklzxcvbnmqwerty";
    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothRequest bluetoothRequest2;
    RequestQueue queue;

    private Handler mBluetoothHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == BT_MESSAGE_READ) {
                String readMessage = null;
                try {
                    readMessage = new String((byte[]) msg.obj, "UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                getMsg(readMessage);

            }
        }
    };

    Response.Listener<String> responseListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success_t = jsonObject.getBoolean("success1");
                    boolean success_h = jsonObject.getBoolean("success2");
                    boolean success_m = jsonObject.getBoolean("success3");

                    if (success_t) {
                        System.out.println("체온값 저장완료");
                        if (success_h) {
                            System.out.println("심박 값 저장완료");
                            if (success_m) {
                                System.out.println("근전도 값 저장완료");
                            } else {
                                System.out.println("근전도 값 저장실패");
                            }
                        } else {
                            System.out.println("심박 값 저장실패");
                        }
                    } else {
                        System.out.println("체온값 저장실패");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            }
        }

        public void onErrorResponse(VolleyError error) {
            Log.e("TAG", "Error at sign in : " + error.getMessage());
        }
    };

    Response.Listener<String> fbrListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                boolean success = jsonObject.getBoolean("success");
                if (success) {
                    System.out.println("피드백 요청 완료");
                } else {
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    public void getMsg(String msg) {
        this.msg = msg;
    }

    public String returnMsg() {
        if (!msg.equals(null))
            return this.msg;
        else {
            return "null";
        }
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Requestalarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        id = intent.getStringExtra("userID");
        name = intent.getStringExtra("userName");
        docID = intent.getStringExtra("docID");
        /*
        Device_name = intent.getStringExtra("Device");
        if (!Device_name.equals("null_device")) {
            connectSelectedDevice(Device_name);
        }
         */
        serviceIntent = intent;
        return START_NOT_STICKY;
    }



    @Override
    public void onDestroy() {
        serviceIntent = null;
        //setAlarmTimer();
        super.onDestroy();

    }

    protected void setAlarmTimer() {
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.SECOND, 1);
        Intent intent = new Intent(this, ServiceBluetoothReceiver.class);
        intent.putExtra("userID", id);
        intent.putExtra("Device", Device_name);
        intent.putExtra("userName", name);
        intent.putExtra("docID", docID);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
    }

    public void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void test() {
        System.out.println("블루투스 서비스 테스트");
    }

    public void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void setmBluetoothAdapter(BluetoothAdapter mBluetoothAdapter) {
        this.mBluetoothAdapter = mBluetoothAdapter;
    }


    public CharSequence[] getItems() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                return items;
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
                return null;
            }
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    public void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);


            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                Device_name = selectedDeviceName;
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    public class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void run() {
            String[] value;
            byte[] buffer = new byte[1024];
            byte[] empty = new byte[buffer.length];
            int bytes, i = 0, j = 0;
            boolean first = true; // 큐에 처음 들어가는건지 아닌지
            queue = Volley.newRequestQueue(getApplicationContext());

            before_x = 1200;
            before_y = 15700;
            before_z = 4750;

            while (true) {
                SystemClock.sleep(2000); //1초간격

                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        System.arraycopy(empty, 0, buffer, 0, buffer.length);
                        bytes = mmInStream.read(buffer, 0, bytes);

                        if (i == 2) {
                            i = 1;
                        }
                        str1 = new String((byte[]) buffer).split("@")[i++];
                        value = str1.split("/");

                        //value = new String((byte[]) buffer).split("/");



                        if (sensor_count != 5) {


                            accel_x = Integer.parseInt(value[4]);
                            accel_y = Integer.parseInt(value[5]);
                            accel_z = Integer.parseInt(value[6]);


                            ABS = (int) (Math.abs(Math.sqrt(Math.pow(accel_x, 2) + Math.pow(accel_y, 2) + Math.pow(accel_z, 2))
                                    - Math.sqrt(Math.pow(before_x, 2) + Math.pow(before_y, 2) + Math.pow(before_z, 2))));
                            SVM = (int) Math.sqrt(Math.pow(accel_x - before_x, 2) + Math.pow(accel_y - before_y, 2) + Math.pow(accel_z - before_z, 2));
                            if (ABS > 15000) {
                                if (SVM - before_svm > 1000) {
                                    j++;
                                    if(j>2)
                                        call119("true");
                                }
                            } else {
                                call119("false");
                            }
                            before_x = accel_x;
                            before_y = accel_y;
                            before_z = accel_z;
                            before_svm = SVM;


                            sensor_count++;

                        } else if (sensor_count == 5)
                        {
                            accel_x = Integer.parseInt(value[4]);
                            accel_y = Integer.parseInt(value[5]);
                            accel_z = Integer.parseInt(value[6]);

                            ABS = (int) (Math.abs(Math.sqrt(Math.pow(accel_x, 2) + Math.pow(accel_y, 2) + Math.pow(accel_z, 2))
                                    - Math.sqrt(Math.pow(before_x, 2) + Math.pow(before_y, 2) + Math.pow(before_z, 2))));
                            SVM = (int) Math.sqrt(Math.pow(accel_x - before_x, 2) + Math.pow(accel_y - before_y, 2) + Math.pow(accel_z - before_z, 2));
                            if (ABS > 15000) {
                                if (SVM - before_svm > 1000) {
                                    j++;
                                    if(j>2)
                                        call119("true");
                                }
                            } else {
                                call119("false");
                            }
                            before_x = accel_x;
                            before_y = accel_y;
                            before_z = accel_z;
                            before_svm = SVM;


                            sensor_count = 0;


                            Intent intent = new Intent("com.example.action.bluetooth.receive");
                            intent.putExtra("Temp", value[0]);
                            intent.putExtra("Heart", value[1]);
                            intent.putExtra("Muscle_L", value[2]);
                            intent.putExtra("Muscle_R", value[3]);

                            intent.putExtra("ABS", String.valueOf(ABS));
                            intent.putExtra("SVM", String.valueOf(SVM));
                            intent.putExtra("Test", "x"+value[4]+"y"+value[5]+"z"+value[6]);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


                            //ㅇㅕ기서부터 저장코드

                            Double temp = Double.parseDouble(value[0]); //가져온 체온값
                            int heart = Integer.parseInt(value[1]);
                            int Rmuscle = Integer.parseInt(value[2]);
                            int Lmuscle = Integer.parseInt(value[3]);

                            Calendar day = Calendar.getInstance();
                            day.setTime(new Date());
                            day.add(Calendar.DATE, 1);
                            String comparedate = dformat.format(day.getTime()); //하루 더해진 날짜


                            dt = new Date();
                            String date = dformat.format(dt); //날짜
                            String stime = tsformat.format(dt); //시간(초)
                            String time = tformat.format(dt);//시간(분)

                            String en_t = temp + "," + date + "," + stime; // 저장되는 값의 형태 '값 날짜 초단위시간'
                            String en_h = heart + "," + date + "," + stime;
                            String en_m = Rmuscle + "," + Lmuscle + "," + date + "," + stime;


                            if (tempQueue.isFull() && heartQueue.isFull()) { //큐가 꽉 찬 상태이면 전체를 평균내서 비교한다.

                                if (first) { // 처음들어간거면 그냥 전체를 다 더함.
                                    for (int k = 0; k < 11; k++) {
                                        if (k != tempQueue.getFront()) {
                                            t_sum += Double.parseDouble(tempQueue.peek(k).split(",")[0]);
                                        }
                                        if (k != heartQueue.getFront()) {
                                            h_sum += Integer.parseInt(heartQueue.peek(k).split(",")[0]);
                                        }
                                        if (k != muscleQueue.getFront()) {
                                            Rm_sum += Integer.parseInt(muscleQueue.peek(k).split(",")[0]);
                                            Lm_sum += Integer.parseInt(muscleQueue.peek(k).split(",")[1]);
                                        }
                                    }
                                    first = false;
                                } else { //처음 들어간게 아니면 기존의 값에서 마지막 하나를 뺴고 새로운 값을 더한다.
                                    t_sum = t_sum - Double.parseDouble(beforet.split(",")[0]) + temp;
                                    h_sum = h_sum - Integer.parseInt(beforeh.split(",")[0]) + heart;
                                    Rm_sum = Rm_sum - Integer.parseInt(beforem.split(",")[0]) + Rmuscle;
                                    Lm_sum = Lm_sum - Integer.parseInt(beforem.split(",")[0]) + Lmuscle;
                                }

                                t_avg = t_sum / 10; //3분평균값
                                h_avg = h_sum / 10;
                                Rm_avg = Rm_sum / 10;
                                Lm_avg = Lm_sum / 10;


                                if (Rm_avg - Lm_avg >= 300 || Rm_avg - Lm_avg <= -300) {
                                    check_m = true;
                                }

                                switch (mode) { //mode로 바꿔야
                                    case 0:
                                        //
                                        //스레드 중단
                                        break;
                                    case 1: //일반모드
                                        if (t_avg >= 37.5 || t_avg <= 33.0) {
                                            check_t = true;
                                        }
                                        if (h_avg >= 100 || h_avg <= 60) {
                                            check_h = true;
                                        }
                                        break;
                                    case 2: //수면모드
                                        if (t_avg >= 37.5 || t_avg <= 33.0) {
                                            check_t = true;
                                        }
                                        if (h_avg >= 90 || h_avg <= 50) {
                                            check_h = true;
                                        }
                                        break;
                                    case 3: //운동모드
                                        if (t_avg >= 39 || t_avg <= 33.0) {
                                            check_t = true;
                                        }
                                        break;
                                    default:
                                        break;
                                }


                                if (a == 10) {
                                    t_avg = Double.parseDouble(String.format("%.1f", t_avg)); //소숫점자름.
                                    try {
                                        String e_id = aes256Chiper.encoding(id, key);
                                        String e_tavg = aes256Chiper.encoding(Double.toString(t_avg), key);
                                        String e_havg = aes256Chiper.encoding(Integer.toString(h_avg), key);
                                        String e_Rmavg = aes256Chiper.encoding(Integer.toString(Rm_avg), key);
                                        String e_Lmavg = aes256Chiper.encoding(Integer.toString(Lm_avg), key);


                                        bluetoothRequest2 = new BluetoothRequest(e_id, e_tavg, e_havg, e_Rmavg, e_Lmavg, date, time, responseListener);

                                        queue.add(bluetoothRequest2); //일반테이블
                                        a = 0; // 180개가 차야됨. 180마다 한번 저장되도록 하는 변수


                                        System.out.println("리퀘스트넘어가기직전 평균값 : " + t_avg + " : 갯수 : " + a);
                                        System.out.println("----------- 평균값 -----------\n");
                                        System.out.println("\n");

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
                                if (check_t || check_h || check_m) { // 이 평균값이 이상체온이거나, 이상심박이거나, 근육이상일경우
                                    int n = 1;
                                    String t[] = new String[3];
                                    String h[] = new String[3];

                                    if (check_m == true) { // 근육이상이면
                                        System.out.println("자세가 불균형합니다. 바른 자세를 유지해 주세요."); //여기 브로드캐스트로 메인액티비티에 전달 ( 진동알림)
                                    }
                                    if (notnom == false) { // 직전값이 이상값이 아닌 경우
                                        while (n < 11) {
                                            if (n != tempQueue.getFront()) { // n이 현재 삭제되기 직전의 칸(값이 비어있는 칸) 이 아닐경우(픽할수 있는 칸일경우)
                                                t = tempQueue.peek(n).split(",");
                                            }
                                            if (n != heartQueue.getFront()) {
                                                h = heartQueue.peek(n).split(",");
                                            }
                                            if (mode == 3) { // 운동모드일 경우 심박 값을 0으로 넣는다.
                                                h[0] = "0";
                                            }
                                            System.out.println("a값 : " + a);

                                            try {
                                                String e_id = aes256Chiper.encoding(id, key);
                                                String e_t_value = aes256Chiper.encoding(t[0], key);
                                                String e_h_value = aes256Chiper.encoding(h[0], key);

                                                BluetoothRequest bluetoothRequest = new BluetoothRequest(e_id, e_t_value,
                                                        date, time, e_h_value, responseListener);

                                                queue.add(bluetoothRequest);

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

                                            n++;
                                        }
                                        String symptom = "";
                                        if (check_t) {
                                            symptom = "체온이상";
                                        }
                                        if (check_h) {
                                            if (!symptom.equals("")) {
                                                symptom = symptom + " 및 심박이상";
                                            } else {
                                                symptom = "심박이상";
                                            }
                                        }
                                        //피드백요청 디비에 전송

                                        String m = "";
                                        switch (mode) {
                                            case 1:
                                                m = "일반모드";
                                                break;
                                            case 2:
                                                m = "수면모드";
                                                break;
                                            case 3:
                                                m = "운동모드";
                                                break;
                                            default:
                                                break;
                                        }


                                        try {
                                            String e_id = aes256Chiper.encoding(id, MainActivity.key);
                                            String e_date = date;
                                            String e_time = time;
                                            String e_symptom = aes256Chiper.encoding(symptom, MainActivity.key);
                                            String e_mode = aes256Chiper.encoding(m, MainActivity.key);
                                            String e_name = aes256Chiper.encoding(name, MainActivity.key);
                                            String e_new = aes256Chiper.encoding("0", MainActivity.key);
                                            String e_doc = aes256Chiper.encoding(docID, MainActivity.key);

                                            if ("".equals(limit_d)) { //리미트날짜가 등록되어있지 않을 때
                                                FeedbackRequest fbAlarmRequest
                                                        = new FeedbackRequest(e_id, e_date + " " + e_time, e_symptom, e_mode, e_doc, e_name, e_new, fbrListener);
                                                queue.add(fbAlarmRequest);
                                                limit_d = setFbDate();
                                            } else if (limit_d.equals(comparedate) && !(modecheck.contains(mode + ""))) { //오늘피드백 전송 했으나 다른 모드에서 이상일때
                                                FeedbackRequest fbAlarmRequest = new FeedbackRequest(e_id, e_date + " " + e_time, e_symptom, e_mode, e_doc, e_name, e_new, fbrListener);
                                                queue.add(fbAlarmRequest);
                                                modecheck += mode + "";
                                            } else if (limit_d.equals(date) || (!limit_d.equals(comparedate) && !limit_d.equals(date))) { //어제리미트날짜이고 오늘피드백날짜 또는 이틀 이상차이날 때
                                                FeedbackRequest fbAlarmRequest = new FeedbackRequest(e_id, e_date + " " + e_time, e_symptom, e_mode, e_doc, e_name, e_new, fbrListener);
                                                queue.add(fbAlarmRequest);
                                                limit_d = setFbDate();
                                            }
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
                                            notnom = true;
                                        } else{
                                            //현재 가져온 값이 이상값이지만 이미 전에 한번 이상값으로 판단되어 데이터베이스에 저장된 경우,
                                            //다시 180개를 저장하는게 아니라 저장되어야 할 이상값 이후의 갯수를 180개 보다 늘린다.
                                            end++;
                                        }
                                    }
                                    if (notnom == true) { // 바로 직전이 이상체온이었을 경우
                                        String[] t = en_t.split(",");
                                        String[] h = en_h.split(",");

                                        if (mode == 3) { // 운동모드일 경우 심박 값을 0으로 넣는다.
                                            h[0] = "0";
                                        }

                                        try {
                                            String e_id = aes256Chiper.encoding(id, key);
                                            String e_t_sec = aes256Chiper.encoding(t[0], key);
                                            String e_h_sec = aes256Chiper.encoding(h[0], key);

                                            BluetoothRequest bluetoothRequest = new BluetoothRequest(e_id, e_t_sec,
                                                    date, time, e_h_sec, responseListener);

                                            queue.add(bluetoothRequest);

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

                                        b++;
                                        if (b == end) {
                                            notnom = false;
                                            b = 0;
                                            end = 10;
                                        }
                                    }
                                    beforet = tempQueue.dequeue();
                                    beforeh = heartQueue.dequeue();
                                    beforem = muscleQueue.dequeue();
                                    heartQueue.enqueue(en_h);
                                    tempQueue.enqueue(en_t); //그리고 다시 방금 초의 값을 넣음.
                                    muscleQueue.enqueue(en_m);
                                } else { //큐가 아직 포화상태가 아니라면,
                                    heartQueue.enqueue(en_h);
                                    tempQueue.enqueue(en_t); //큐에 1초간격으로 값 삽입.
                                    muscleQueue.enqueue(en_m);


                                }
                                a++;



                            }
                        }
                    }catch(IOException e){
                        break;
                    }



                }


            }



        private void call119(String value) {
            String tel = "tel:119";
            Intent showIntent = new Intent("tell");
            showIntent.putExtra("check",value);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(showIntent);
        }
        private String setFbDate() { // 제한 날짜 구하기
            Calendar date = Calendar.getInstance();
            date.setTime(new Date());
            date.add(Calendar.DATE,1);
            String day = dformat.format(date.getTime());
            modecheck = "";
            modecheck += mode+"";
            return day;
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void Requestalarm() {
        Intent intent = new Intent("com.example.action.bluetoothservice.alarm");
        intent.putExtra("alarm_request", "vibrate");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }



    interface QueueInterface {

        public abstract boolean isFull(); // 큐가 포화상태인지 검사하는 메소드

        public abstract boolean isEmpty(); // 큐가 비어있는지 검사하는 메소드

        public abstract void enqueue(String val); // 큐에 데이터를 삽입하는 메소드

        public abstract String dequeue(); // 큐에 들어있는 데이터를 삭제하는 메소드

        public abstract String peek(int index); // 다음번 dequeue 될 데이터 출력하는 메소드

    }

    public class MyQueue implements QueueInterface {

        int front; // 출력 포인터
        int rear; // 삽입 포인터
        int Qsize; // 전체 큐의 사이즈
        String[] QArray; // Qsize를 이용하여 전체 배열 생성

        public MyQueue(int Qsize) { // 큐를 생성하는 메소드
            front = 0; //
            rear = 0; // 맨처음 출력,삽입 포인터가 큐의 0번지를 가리키고 있다.
            this.Qsize = Qsize;
            QArray = new String[Qsize];
            System.out.println("사이즈는 : " + Qsize);
        }

        public int getFront() {
            return front;
        }

        public int getRear() {
            return rear;
        }

        @Override
        public boolean isFull() { // 큐가 포화 상태인지 검사하는 메소드
            if (((rear + 1) % this.Qsize) == front) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isEmpty() { // 큐가 비어있는지 검사하는 메소드.
            return rear == front; // rear와 front는 데이터가 삽입되기 전에만 같은 위치를 가리키기 때문에 큐가 비어있는 상태검사에 사용.
        }

        @Override
        public void enqueue(String val) {
            if (isFull()) { // 큐가 가득차 있지 않다면 삽입.
                System.out.println("큐가 꽉 차 있습니다.");
            } else {
                rear = (++rear) % this.Qsize; // 삽입포인터랑 삭제포인터 1차이남. 그리고 3->0으로 가는경우를 생각해야함
                QArray[rear] = val; // 삽입포인터 rear가 가리키는 공간에 데이터 삽입.
                System.out.println(val + "를 " + rear + "번째에" + " 삽입");
            }
        }

        @Override
        public String dequeue() { //마지막 값 ( 가장 처음 들어온 값 삭제 )
            if (isEmpty()) {
                return "0";
            } else {
                front = (++front) % this.Qsize;
                return QArray[(front) % Qsize];
            }
        }

        @Override
        public String peek(int index) {
            System.out.println(QArray[(front + 1) % this.Qsize]);
            return QArray[index];
        }

    }
}