package com.example.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.ReservationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@RequiresApi(api = Build.VERSION_CODES.N)
public class Frag_main extends Fragment {
    private View view;
    private String name, blood, age ,r_date,r_time,d_id,hospital, id, str;
    private TextView name_tf;
    private TextView age_tf;
    private TextView blood_tf;
    private TextView temp, heart,muscle ;
    private TextView res_doc,res_date, res_major;
    private int muscle_avg_L = 0;
    private int muscle_avg_R = 0;
    private AES256Chiper aes256Chiper;

    private SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd");
    private SimpleDateFormat format2 = new SimpleDateFormat("HH:mm");


    private musclebl2Fragment musclebl2Fragment;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    // 화면을 구성할때 호출되는 부분
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_main , container , false);

        name_tf = view.findViewById(R.id.name_tf);
        age_tf = view.findViewById(R.id.age_tf);
        blood_tf = view.findViewById(R.id.blood_tf);
        res_doc = view.findViewById(R.id.doctor_r_tf);
        res_date = view.findViewById(R.id.date_r_tf);
        res_major = view.findViewById(R.id.major_r_tf);
        muscle = view.findViewById(R.id.muscle_tf);

        musclebl2Fragment = new musclebl2Fragment();


        if (muscle_avg_L == 0 && muscle_avg_R == 0){
            muscle.setText("기준치가 없습니다");
        }
        else{
            muscle.setText(String.valueOf("좌 : "+muscle_avg_L+"우 : "+muscle_avg_R));
        }


        name = getArguments().getString("Name");
        blood = getArguments().getString("Blood");
        age = getArguments().getString("Age");
        id = getArguments().getString("userID");

        name_tf.setText(name+ " 님");
        blood_tf.setText(blood+" 형");
        age_tf.setText("만 "+age+" 세");

        temp = view.findViewById(R.id.temperature);
        heart = view.findViewById(R.id.heart);

        aes256Chiper = new AES256Chiper();

        SharedPreferences auto = requireActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = auto.edit();

        editor.putString("TEST","name");
        editor.commit();


        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int i = 0 ;
                    Date ymd = new Date();
                    String today = format1.format(ymd);
                    String now = format2.format(ymd);
                    StringBuffer sb = new StringBuffer();

                    sb.append(today);
                    char tmp = sb.charAt(5);
                    if(tmp == '0'){
                        sb.deleteCharAt(5);
                    }
                    today = sb.toString();
                    System.out.println("최근예약 오늘날짜 : "+today);


                    while(i < jsonArray.length()){

                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        boolean success = jsonObject.getBoolean("check");
                        if (success) {//예약이 있을경우
                            String r_date = aes256Chiper.decoding(jsonObject.getString("resDate"),MainActivity.key);

                            System.out.println("최근예약 예약날짜 : "+r_date);

                            String todays[] = today.split("/");
                            String nows[] = now.split(":");
                            int y = Integer.parseInt(todays[0]);
                            int m = Integer.parseInt(todays[1]);
                            int d = Integer.parseInt(todays[2]);
                            int h = Integer.parseInt(nows[0])+1;
                            int mn = Integer.parseInt(nows[1]);

                            String r_time = aes256Chiper.decoding(jsonObject.getString("resTime"),MainActivity.key);
                            String d_name = aes256Chiper.decoding(jsonObject.getString("docName"),MainActivity.key);
                            String d_major =aes256Chiper.decoding(jsonObject.getString("docMajor"),MainActivity.key);

                            System.out.println(r_date);

                            String dt = r_date + " " + r_time;

                            String dates[] = r_date.split("/");
                            int r_y = Integer.parseInt(dates[0]);
                            int r_m = Integer.parseInt(dates[1]);
                            int r_d = Integer.parseInt(dates[2]);
                            String time[] = r_time.split(":");
                            int r_h = Integer.parseInt(time[0]);
                            int r_mn = Integer.parseInt(time[1]);



                            System.out.println("최근예약");
                            System.out.println("오늘 : "+y+" / "+m+" / "+d+" // "+h+" : "+mn);
                            System.out.println("날짜 : "+r_y+" / "+r_m+" / "+r_d+" // "+r_h+" : "+r_mn);


                            if (r_y < y) {//최근예약년도가 오늘보다 이전일 떄
                                setRecentlyRes(false, d_name, dt, d_major);
                            }
                            else if (r_y > y) {//최근 예약 년도가 오늘보다 미래일
                                setRecentlyRes(true, d_name, dt, d_major);
                            }
                            else {//최근 예약 년도가 오늘과 같을 떄
                                if (r_m < m) {//최근 예약 월이 오늘보다 이전일 때
                                    setRecentlyRes(false, d_name, dt, d_major);
                                }
                                else if (r_m == m) {//최근 예약 월이 오늘과 같은 월일떄
                                    if (r_d < d) {//최근 예약 일이 오늘보다 이전일 때
                                        setRecentlyRes(false, d_name, dt, d_major);
                                    }
                                    else if (r_d == d) {//최근 예약 일이 오늘일 떄
                                        if(r_h > h || (r_h == h && r_mn > mn)){
                                            setRecentlyRes(true, d_name, dt, d_major);
                                            break;
                                        }
                                        else{ setRecentlyRes(false, d_name, dt, d_major); }
                                    }
                                    else {//최근 예약 일이 오늘보다 이후일 때
                                        setRecentlyRes(true, d_name, dt, d_major);
                                        break;
                                    }
                                }
                                else { //최근 예약 월이 오늘보다 이후일 때
                                    setRecentlyRes(true, d_name, dt, d_major);
                                    break;
                                }
                            }
                        }
                        else {
                            setRecentlyRes(false, "", "", "");
                        }
                        i++;

                    }
                    if(jsonArray.length() == 0){
                        setRecentlyRes(false, "", "", "");
                    }
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
            String e_id = aes256Chiper.encoding(id, MainActivity.key);
            ReservationRequest recentlyresRequest = new ReservationRequest(1,e_id, responseListener);
            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(recentlyresRequest);
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




        res_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).replaceFragment(ConfirmFragment.getInstance());
            }
        });
        res_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).replaceFragment(ConfirmFragment.getInstance());
            }
        });
        res_major.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).replaceFragment(ConfirmFragment.getInstance());
            }
        });

        muscle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("근전도 기준치를 잡으시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getSupportFragmentManager()
                                .beginTransaction().replace(R.id.main_layout, musclebl2Fragment).commit();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });
        return view;
    }

    public void setMuscle_avg(int muscle_avg_L, int muscle_avg_R) {
        this.muscle_avg_L = muscle_avg_L;
        this.muscle_avg_R = muscle_avg_R;
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
            temp.clearComposingText();
            heart.clearComposingText();
            temp.setText(intent.getStringExtra("Temp"));
            heart.setText(intent.getStringExtra("Heart"));
            Toast.makeText(context, intent.getStringExtra("Test")+"ABS"+intent.getStringExtra("ABS")+"SVM"+intent.getStringExtra("SVM"), Toast.LENGTH_SHORT).show();
            if (muscle_avg_L != 0 && muscle_avg_R !=0){
                // 기준값 있을 때 값 가져올거임
            }
        }
    };

    public void setRecentlyRes(boolean check, String doc, String date, String major){
        if(check == true){
            res_doc.setText(doc);
            res_date.setText(date);
            res_major.setText(major);
        }
        else{
            res_doc.setText(" -------  ");
            res_date.setText("다가오는 예약이 없습니다.");
            res_major.setText("  ------- ");
        }
    }

}
