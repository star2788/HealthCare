package com.example.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.ReservationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Frag_calendar extends Fragment {

    private View view;
    private CalendarView calendarView;
    private Button btn1030, btn1100, btn1330, btn1400, btn1430, btn1500, btn1530, btn1600,res_btn;
    private TextView date_tf, time_tf;
    private Context ct;
    String id = "";
    String d = "";
    String t = "";
    String doc = "";
    private String d_room = "";
    private String d_hos = "";

    private AES256Chiper aes256Chiper;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_calendar , container , false);

        ct = container.getContext();
        calendarView = view.findViewById(R.id.calendarView);

        Date today = new Date();
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        c.setTime(today);

        long minDate = c.getTime().getTime()+86400000L;
        long maxDate = minDate+2678400000L;

        calendarView.setMinDate(minDate);
        calendarView.setMaxDate(maxDate);
        RequestQueue queue = Volley.newRequestQueue(ct);

        btn1030 = view.findViewById(R.id.ten30);
        btn1100 = view.findViewById(R.id.eleven);
        btn1330 = view.findViewById(R.id.thirteen30);
        btn1400 = view.findViewById(R.id.fourteen);
        btn1430 = view.findViewById(R.id.fourteen30);
        btn1500 = view.findViewById(R.id.fifteen);
        btn1530 = view.findViewById(R.id.fifteen30);
        btn1600 = view.findViewById(R.id.sixteen);
        res_btn = view.findViewById(R.id.res_btn);
        date_tf = view.findViewById(R.id.res_date);
        time_tf = view.findViewById(R.id.res_time);

        Response.Listener<String> responseListener = new Response.Listener<String>() { //예약 인서트 리스너
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if(success){

                        AlertDialog.Builder dialog = new AlertDialog.Builder(ct);
                        dialog.setMessage("예약이 완료되었습니다.");
                        dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity)getActivity()).replaceFragment(ConfirmFragment.getInstance());
                            }
                        });
                        dialog.show();
                    }
                    else{
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };







        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayofmonth) {
                initBtn();

                String id = getArguments().getString("userID");
                String date = year+"/"+(month+1)+"/"+dayofmonth;
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            int i = 0;
                            while(i < jsonArray.length()){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                boolean success = jsonObject.getBoolean("success");
                                String time = jsonObject.getString("resTime");
                                System.out.println("시간 : "+time);
                                if(success){
                                    switch (time){
                                        case "10:30":
                                            btn1030.setEnabled(false);
                                            break;
                                        case "11:00":
                                            btn1100.setEnabled(false);
                                            break;
                                        case "13:30":
                                            btn1330.setEnabled(false);
                                            break;
                                        case "14:00":
                                            btn1400.setEnabled(false);
                                            break;
                                        case "14:30":
                                            btn1430.setEnabled(false);
                                            break;
                                        case "15:00":
                                            btn1500.setEnabled(false);
                                            break;
                                        case "15:30":
                                            btn1530.setEnabled(false);
                                            break;
                                        case "16:00":
                                            btn1600.setEnabled(false);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                                else{
                                }

                                i++;
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                ReservationRequest reserbtnRequest = new ReservationRequest(1,id,date, responseListener);
                RequestQueue queue = Volley.newRequestQueue(ct);
                queue.add(reserbtnRequest);

                setDate(year,month+1,dayofmonth);

            }
        });

        btn1030.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1030);
            }
        });

        btn1100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1100);
            }
        });

        btn1330.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1330);
            }
        });

        btn1400.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1400);
            }
        });

        btn1430.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1430);
            }
        });

        btn1500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1500);
            }
        });

        btn1530.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1530);
            }
        });

        btn1600.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(btn1600);
            }
        });

        res_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                String tmp = date_tf.getText().toString();

                //날짜 디비 저장 위해 전처리(후에 처리 용이하게 하기 위함)
                tmp = tmp.replaceAll(" ","");
                String ymd[] = tmp.split("년");
                String year = ymd[0];
                String month = ymd[1].split("월")[0];
                String day = ymd[1].split("월")[1].split("일")[0];

                String date = year+"/"+month+"/"+day;
                String time = time_tf.getText().toString();

                String idd = getArguments().getString("userID");
                String d_id = getArguments().getString("conDoc");
                System.out.println("의사아이디 : "+d_id);

                if(!("".equals(date)) && !("".equals(time))){

                    aes256Chiper = new AES256Chiper();
                    try {
                        d = aes256Chiper.encoding(date,MainActivity.key);
                        t = aes256Chiper.encoding(time, MainActivity.key);
                        id = aes256Chiper.encoding(idd, MainActivity.key);
                        doc = aes256Chiper.encoding(d_id,MainActivity.key);

                        ReservationRequest reservationRequest = new ReservationRequest(id,t,d,doc,responseListener);
                        queue.add(reservationRequest);


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
                else if("".equals(date)){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ct);
                    dialog.setMessage("날짜를 선택해주세요.");
                    dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    dialog.show();
                }
                else if("".equals(time)){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ct);
                    dialog.setMessage("시간을 선택해주세요.");
                    dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    dialog.show();
                }


                try {
                    String e_id = aes256Chiper.encoding(id,MainActivity.key);
                    String e_date = aes256Chiper.encoding(date, MainActivity.key);

                    ReservationRequest reserbtnRequest = new ReservationRequest(1,e_id,e_date, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(ct);
                    queue.add(reserbtnRequest);
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
        });

        return view;
    }

    public void setDate( int y, int m, int d){
        String date = y+"년 "+m+"월 "+d+"일";
        date_tf.setText(date);
    }
    public void setTime(Button btn){
        String time = btn.getText().toString();
        time_tf.setText(time);
    }
    public void initBtn(){
        btn1030.setEnabled(true);
        btn1100.setEnabled(true);
        btn1330.setEnabled(true);
        btn1400.setEnabled(true);
        btn1430.setEnabled(true);
        btn1500.setEnabled(true);
        btn1530.setEnabled(true);
        btn1600.setEnabled(true);
    }


}
