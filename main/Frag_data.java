package com.example.main;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.DataRequest;
import com.example.main.Request.FeedbackRequest;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

@RequiresApi(api = Build.VERSION_CODES.N)
public class Frag_data extends Fragment {

    private LineChart lineChart;
    private ArrayList<Feedback> feedbacks;
    private ListView personalListView;
    private static PersonalAdapter perAdapter;

    private Calendar myCalendar = Calendar.getInstance();
    private Calendar myCalendar2 = Calendar.getInstance();
    private Calendar minDate = Calendar.getInstance();
    private Calendar maxDate = Calendar.getInstance();

    private Date date;
    private SimpleDateFormat simpleDateFormat;
    private String strnow, id;
    private String[] format;
    private String[] time;

    private TextView fromdate;
    private TextView todate;
    private TextView fromtime;
    private TextView totime;
    private Button updatebtn_m, updatebtn_h, updatebtn_t;
    private Context ct;
    private Bundle mBundle;
    private ArrayList<Entry> entry_chart, entry_chart2;
    private LineData chartData;
    private XAxis xAxis;
    private YAxis yRAxis, yLAxis;
    private Description description;
    private String fd,td,ft,tt;
    private boolean check = false, check2 = false;
    private AES256Chiper aes256Chiper;


    private View view;
    private ArrayList<String> data;

    DatePickerDialog.OnDateSetListener myDatePicker = new DatePickerDialog.OnDateSetListener(){
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel1();
        }
    };

    DatePickerDialog.OnDateSetListener myDatePicker2 = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar2.set(Calendar.YEAR, year);
            myCalendar2.set(Calendar.MONTH, month);
            myCalendar2.set(Calendar.DAY_OF_MONTH,dayOfMonth);
            updateLabel2();
        }
    };

    Response.Listener<String> fbrListener = new Response.Listener<String>() { //피드백 가져오는 리스너
        @Override
        public void onResponse(String response) {
            try {
                feedbacks = new ArrayList<>();
                perAdapter = new PersonalAdapter(getContext(), feedbacks);
                personalListView.setAdapter(perAdapter);
                aes256Chiper = new AES256Chiper();

                JSONArray jsonArray = new JSONArray(response);
                int i = 0;
                while(i < jsonArray.length()){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    boolean success = jsonObject.getBoolean("success");
                    if(success){
                        int seq = i+1;
                        String feedback = aes256Chiper.decoding(jsonObject.getString("fContent"),MainActivity.key);
                        String date = jsonObject.getString("fDate");
                        String nndate = jsonObject.getString("fnnDate");
                        String symptom = aes256Chiper.decoding(jsonObject.getString("fSymptom"),MainActivity.key);
                        String doctor = aes256Chiper.decoding(jsonObject.getString("docName"),MainActivity.key);
                        feedbacks.add(new Feedback(i+1, date, nndate, symptom,doctor,feedback ));
                    }
                    else{

                    }
                    i++;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_data , container , false);
        ct = container.getContext();
        view = rootView;

        System.out.println(data);

        fromdate = rootView.findViewById(R.id.fromdate);
        todate = rootView.findViewById(R.id.todate);
        fromtime = rootView.findViewById(R.id.fromtime);
        totime = rootView.findViewById(R.id.totime);
        updatebtn_h = rootView.findViewById(R.id.update_btn); //심전도
        updatebtn_m = rootView.findViewById(R.id.update_btn2);  //근전도
        updatebtn_t = rootView.findViewById(R.id.update_btn3); //체
        personalListView = (ListView)rootView.findViewById(R.id.f_list);
        id = getArguments().getString("userID");


        lineChart = (LineChart) rootView.findViewById(R.id.datachart);
        chartData = new LineData();


        xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.enableGridDashedLine(8, 24, 0);

        yLAxis = lineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        yRAxis = lineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        description = new Description();
        description.setText("");

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.animateY(2000, Easing.EasingOption.EaseInCubic);

        date = new Date(System.currentTimeMillis()); //system으로부터 현재시간 가져오고 Date객체에 저장
        simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        strnow = simpleDateFormat.format(date);

        String current_time[] = strnow.split("/");
        maxDate.set(Integer.parseInt(current_time[0]),Integer.parseInt(current_time[1])-1,Integer.parseInt(current_time[2]));

        fromdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check == true){
                    fromdate.setText("");
                    todate.setText("");
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(ct, myDatePicker,
                        myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime().getTime());
                datePickerDialog.show();
                fromtime.setEnabled(true);
                totime.setEnabled(true);
                check = true;
            }
        });
        todate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("".equals(fromdate.getText().toString())){
                    Toast.makeText(getContext(),"시작 날짜를 먼저 선택해주세요.",Toast.LENGTH_SHORT).show();
                }
                else{
                    DatePickerDialog datePickerDialog2 = new DatePickerDialog(ct, myDatePicker2, myCalendar2.get(Calendar.YEAR), myCalendar2.get(Calendar.MONTH), myCalendar2.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog2.getDatePicker().setMaxDate(maxDate.getTime().getTime());
                    datePickerDialog2.getDatePicker().setMinDate(minDate.getTime().getTime());
                    datePickerDialog2.show();
                    fromtime.setEnabled(true);
                    totime.setEnabled(true);
                    check = true;

                }
            }
        });


        fromtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check2 == true){
                    fromtime.setText("");
                    totime.setText("");
                }
                String tmp = fromdate.getText().toString();
                String tmp2 = todate.getText().toString();
                if("".equals(tmp) || "".equals(tmp2)){
                    Toast.makeText(getContext(), "날짜를 우선 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(!(tmp.equals(tmp2))){
                    Toast.makeText(getContext(), "시간선택이 필요하지 않습니다. ", Toast.LENGTH_SHORT).show();
                    fromtime.setEnabled(false);
                    totime.setEnabled(false);
                }
                else{
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(android.icu.util.Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(android.icu.util.Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(ct, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            String state = "AM";
                            // 선택한 시간이 12를 넘을경우 "PM"으로 변경 및 -12시간하여 출력 (ex : PM 6시 30분)
                            if (selectedHour > 12) {
                                selectedHour -= 12;
                                state = "PM";
                            }
                            // EditText에 출력할 형식 지정
                            fromtime.setText(state + " " + selectedHour + "시 " + selectedMinute + "분");
                        }
                    }, hour, minute, false); // true의 경우 24시간 형식의 TimePicker 출현
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                    check2 = true;

                }
            }
        });

        totime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String tmp = fromdate.getText().toString();
                String tmp2 = todate.getText().toString();

                if("".equals(tmp) || "".equals(tmp2)){
                    Toast.makeText(getContext(), "날짜를 우선 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(!(tmp.equals(tmp2))){
                    Toast.makeText(getContext(), "시간선택이 필요하지 않습니다. ", Toast.LENGTH_SHORT).show();
                    fromtime.setEnabled(false);
                    totime.setEnabled(false);
                }
                else{
                    Calendar mcurrentTime = Calendar.getInstance();
                    int hour = mcurrentTime.get(android.icu.util.Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(android.icu.util.Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(ct, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                            String state = "AM";
                            // 선택한 시간이 12를 넘을경우 "PM"으로 변경 및 -12시간하여 출력 (ex : PM 6시 30분)
                            if (selectedHour > 12) {
                                selectedHour -= 12;
                                state = "PM";
                            }
                            // EditText에 출력할 형식 지정
                            totime.setText(state + " " + selectedHour + "시 " + selectedMinute + "분");
                        }
                    }, hour, minute, false); // true의 경우 24시간 형식의 TimePicker 출현
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();
                    check2 = true;

                }
            }
        });

        updatebtn_m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                init_datafrag();

                if(!("".equals(fd)) && !("".equals(td))){ //날짜가 모두선택 되었을 때,
                    updatebtn_m.setBackgroundColor(Color.LTGRAY);
                    updatebtn_h.setBackgroundColor(getResources().getColor(R.color.theme_color));
                    updatebtn_t.setBackgroundColor(getResources().getColor(R.color.theme_color));

                    try {
                        String e_id = aes256Chiper.encoding(id, MainActivity.key);
                        String e_fd = fd;
                        String e_td = td;

                        if(fd.equals(td)) { //선택한 날짜가 같으면 (확인 구간이 하루이면)
                            if (!("".equals(ft)) && !("".equals(tt))) { //시간이 선택된것을 확인한다.
                                String e_ft = setTime(ft);
                                String e_tt = setTime(tt);
                                FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, e_ft, e_tt, fbrListener);
                                DataRequest mDataRequest = new DataRequest(1, e_id, e_fd, e_td, e_ft, e_tt, chartrListener);
                                setRequest(1,1,feedbackRequest, mDataRequest);
                            }
                            else{ // 날짜 같음, 시간 선택 엑스
                                DataRequest mDataRequest = new DataRequest(1,e_id, e_fd,e_td, chartrListener);
                                FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, fbrListener);
                                setRequest(1,1,feedbackRequest, mDataRequest);
                            }
                        }
                        else {//날짜 다를때, 시간선택 이미 못하게 막아뒀음.
                            DataRequest mDataRequest = new DataRequest(1,e_id, e_fd, e_td, chartrListener);
                            FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, fbrListener);
                            setRequest(1,1,feedbackRequest, mDataRequest);
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

                }

                else{
                    Toast.makeText(getContext(),"날짜 구간을 선택해주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        updatebtn_t.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                init_datafrag();

                if(!("".equals(fd)) && !("".equals(td))){ //날짜가 모두선택 되었을 때,
                    updatebtn_t.setBackgroundColor(Color.LTGRAY);
                    updatebtn_h.setBackgroundColor(getResources().getColor(R.color.theme_color));
                    updatebtn_m.setBackgroundColor(getResources().getColor(R.color.theme_color));


                    try {
                        String e_id = aes256Chiper.encoding(id, MainActivity.key);
                        String e_fd = fd;
                        String e_td = td;



                        if(fd.equals(td)) { //선택한 날짜가 같으면 (확인 구간이 하루이면)
                            if (!("".equals(ft)) && !("".equals(tt))) { //시간이 선택된것을 확인한다.
                                String e_ft = setTime(ft);
                                String e_tt = setTime(tt);
                                FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, e_ft, e_tt, fbrListener);
                                DataRequest dataRequest = new DataRequest(e_id, e_fd, e_td, e_ft, e_tt, chartrListener);
                                setRequest('t',feedbackRequest, dataRequest);
                            }
                            else{ // 날짜 같음, 시간 선택 엑스
                                DataRequest dataRequest = new DataRequest(e_id,e_fd, e_td, chartrListener);
                                FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, fbrListener);
                                setRequest('t',feedbackRequest, dataRequest);
                            }
                        }
                        else {//날짜 다를때, 시간선택 이미 못하게 막아뒀음.
                            DataRequest dataRequest = new DataRequest(e_id, e_fd, e_td, chartrListener);
                            FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, fbrListener);
                            setRequest('t',feedbackRequest, dataRequest);
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



                }

                else{
                    Toast.makeText(getContext(),"날짜 구간을 선택해주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        updatebtn_h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init_datafrag();

                if(!("".equals(fd)) && !("".equals(td))){ //날짜가 모두선택 되었을 때,
                    updatebtn_h.setBackgroundColor(Color.LTGRAY);
                    updatebtn_m.setBackgroundColor(getResources().getColor(R.color.theme_color));
                    updatebtn_t.setBackgroundColor(getResources().getColor(R.color.theme_color));


                    try {
                        String e_id = aes256Chiper.encoding(id, MainActivity.key);
                        String e_fd = fd;
                        String e_td = td;


                        if(fd.equals(td)) { //선택한 날짜가 같으면 (확인 구간이 하루이면)
                            if (!("".equals(ft)) && !("".equals(tt))) { //시간이 선택된것을 확인한다.
                                String e_ft = setTime(ft);
                                String e_tt = setTime(tt);
                                FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, e_ft, e_tt, fbrListener);
                                DataRequest hDataRequest = new DataRequest('1',1,e_id, e_fd, e_td, e_ft, e_tt, chartrListener);
                                setRequest(1,feedbackRequest, hDataRequest);
                            }
                            else{ // 날짜 같음, 시간 선택 엑스
                                DataRequest hDataRequest = new DataRequest('1',e_id, e_fd,e_td, chartrListener);
                                FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, fbrListener);
                                setRequest(1,feedbackRequest, hDataRequest);
                            }
                        }
                        else {//날짜 다를때, 시간선택 이미 못하게 막아뒀음.
                            DataRequest hDataRequest = new DataRequest('1',e_id, e_fd, e_td, chartrListener);
                            FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, e_fd, e_td, fbrListener);
                            setRequest(1,feedbackRequest, hDataRequest);
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


                }

                else{
                    Toast.makeText(getContext(),"날짜 구간을 선택해주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        personalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                mBundle = new Bundle();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                mBundle.putString("userID",id);
                mBundle.putString("docName",feedbacks.get(position).getDoc());
                mBundle.putString("fContent",feedbacks.get(position).getContent());
                mBundle.putString("fDate",feedbacks.get(position).getDate());
                mBundle.putString("fnnDate",feedbacks.get(position).getNndate());
                mBundle.putString("fSymptom",feedbacks.get(position).getSymptom());
                FbconFragment fbconFragment = new FbconFragment();
                fbconFragment.setArguments(mBundle);
                transaction.replace(R.id.main_layout,fbconFragment);
                transaction.commit();
            }
        });


        return rootView;
    }

    private void setRequest(char t,FeedbackRequest feedbackRequest, DataRequest dataRequest) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(feedbackRequest);
        queue.add(dataRequest);
    }
    private void setRequest(int h, FeedbackRequest feedbackRequest, DataRequest hDataRequest) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(feedbackRequest);
        queue.add(hDataRequest);
    }
    private void setRequest(int m,int mm, FeedbackRequest feedbackRequest, DataRequest mDataRequest) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(feedbackRequest);
        queue.add(mDataRequest);
    }

    private void init_datafrag() {

        if(chartData != null){
            lineChart.fitScreen();
            chartData.clearValues();
            xAxis.setValueFormatter(null);
            lineChart.notifyDataSetChanged();
            lineChart.clear();
            lineChart.invalidate();
        }
        fd = fromdate.getText().toString();
        td = todate.getText().toString();
        ft = fromtime.getText().toString();
        tt = totime.getText().toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateLabel1() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);
        TextView fd = (TextView) view.findViewById(R.id.fromdate);
        fd.setText(sdf.format(myCalendar.getTime()));
        String current_time[] = fd.getText().toString().split("/");
        minDate.set(Integer.parseInt(current_time[0]),Integer.parseInt(current_time[1])-1,Integer.parseInt(current_time[2]));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateLabel2() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

        TextView td = (TextView) view.findViewById(R.id.todate);
        td.setText(sdf.format(myCalendar2.getTime()));
    }


    public void setData(String data){
        this.data.add(data);
    }

    public String setTime(String time){
        String t;
        System.out.println("tststs : "+time);
        if(time.contains("PM")) {
            int tmp = Integer.parseInt(time.split(" ")[1].split("시")[0]);
            if (tmp < 12) {
                tmp += 12;
            }
            t = tmp + ":" + time.split(" ")[2].split("분")[0];
        }
        else{
            t = time.split(" ")[1].split("시")[0] + ":" + time.split(" ")[2].split("분")[0];
        }
        System.out.println("ttttststst : "+t);
        return t;
    }

    public class MyValueFormatter implements IAxisValueFormatter {
        private String[] mValues;
        // 생성자 초기화
        public MyValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int) value];
        }
    }


    Response.Listener<String> chartrListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            if(response!=null)
                try {
                    System.out.println("센서값");
                    entry_chart = new ArrayList<>();
                    entry_chart2 = new ArrayList<>();
                    JSONArray jsonArray = new JSONArray(response);
                    System.out.println("엔트리차트 ; "+entry_chart.size()+" // 제이선어레이 : "+jsonArray.length());
                    int i = 0;
                    String date ="";
                    int r = jsonArray.length();
                    format = new String[r];
                    time = new String[r];
                    String sensor = "";
                    while (i < r) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            time[i] = jsonObject.getString("MTime");
                            String tmpd = jsonObject.getString("MDate");

                            if (!date.equals(tmpd)) {
                                format[i] = tmpd;
                                date = tmpd;
                            }
                            else{
                                format[i] = "";
                            }
                            String ss = jsonObject.getString("sensor");
                            System.out.println("센서값 : "+ss);

                            if(ss.equals("temp")){
                                System.out.println("센서!!! : "+ss);

                                String val = aes256Chiper.decoding(jsonObject.getString("MVal"),MainActivity.key);
                                entry_chart.add(new Entry(i, Float.parseFloat(val)));
                                sensor = "체온";
                                LineDataSet lineDataSet = new LineDataSet(entry_chart,sensor);

                            }
                            else if(ss.equals("heart")){
                                System.out.println("센서!!! : "+ss);

                                String val = aes256Chiper.decoding(jsonObject.getString("MVal"),MainActivity.key);
                                entry_chart.add(new Entry(i, Float.parseFloat(val)));
                                sensor = "심전도";
                                LineDataSet lineDataSet = new LineDataSet(entry_chart,sensor);
                            }
                            else if(ss.equals("muscle")){
                                System.out.println("센서!!! : "+ss);
                                String valr = aes256Chiper.decoding(jsonObject.getString("MValR"),MainActivity.key);
                                String vall = aes256Chiper.decoding(jsonObject.getString("MValL"),MainActivity.key);
                                entry_chart.add(new Entry(i,Integer.parseInt(valr)));
                                entry_chart2.add(new Entry(i,Integer.parseInt(vall)));
                                System.out.println("좌근전도 : "+entry_chart2.size());

                                sensor = "R근전도";
                            }



                        }
                        i++;
                    }
                    xAxis.setLabelCount(i, true);
                    xAxis.setValueFormatter(new MyValueFormatter(format));
                    LineDataSet lineDataSet2 = null;
                    LineDataSet lineDataSet = new LineDataSet(entry_chart,sensor);

                    if(sensor.equals("R근전도")){
                        System.out.println("좌근전도ddddd : ");
                        lineDataSet2 = new LineDataSet(entry_chart2,"L"+sensor);
                        lineDataSet2.setDrawHorizontalHighlightIndicator(false);
                        lineDataSet2.setDrawHighlightIndicators(false);
                        lineDataSet2.setDrawValues(false);
                        lineDataSet2.setCircleColor(Color.parseColor("#FF6988c7"));
                        lineDataSet2.setCircleColorHole(Color.BLUE);
                        lineDataSet2.setColor(Color.parseColor("#FF6988c7"));
                    }


                    lineDataSet.setDrawHorizontalHighlightIndicator(false);
                    lineDataSet.setDrawHighlightIndicators(false);
                    lineDataSet.setDrawValues(false);
                    lineDataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
                    lineDataSet.setCircleColorHole(Color.BLUE);
                    lineDataSet.setColor(Color.parseColor("#FFA1B4DC"));

                    MyMarkerView marker = new MyMarkerView(getContext(),R.layout.markerviewtext);
                    marker.setChartView(lineChart);
                    lineChart.setMarker(marker);


                    chartData.addDataSet(lineDataSet);
                    if(sensor.equals("R근전도")){
                        System.out.println("좌근전도ddddd : "+lineDataSet2);
                        chartData.addDataSet(lineDataSet2);
                    }
                    lineChart.setData(chartData);
                    lineChart.invalidate();

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


    public class MyMarkerView extends MarkerView {

        private TextView tvContent;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);

            tvContent = (TextView)findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {

            if (e instanceof CandleEntry) {

                CandleEntry ce = (CandleEntry) e;

                tvContent.setText(Html.fromHtml(ce.getHigh() + "<br />" + time[(int)ce.getLow()]));
            } else {

                tvContent.setText(Html.fromHtml(e.getY() + "<br />" + time[(int)e.getX()]));
            }

            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }

}

