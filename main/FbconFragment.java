package com.example.main;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.DataRequest;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class FbconFragment extends Fragment {
    private LineChart lineChart;
    private String doc, nndate, date, symptom, content, id, ftime, ttime;
    private TextView doc_tf, date_tf, symptom_tf,content_tf;
    private String format[];
    private String time[];

    private ArrayList<Entry> tentry;
    private ArrayList<Entry> hentry;
    private ArrayList<Entry> rmentry;
    private ArrayList<Entry> lmentry;

    private LineDataSet hline, rmline, lmline, tline;

    private LineData chartData;
    private XAxis xAxis;
    private YAxis yRAxis, yLAxis;
    private Description description;
    private boolean mc = false, tc = false, hc = false;
    private boolean formatch = true;
    private AES256Chiper aes256Chiper;



    public static FbconFragment getInstance() {
        return new FbconFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootview = inflater.inflate(R.layout.fragment_fbcon, container, false);

        doc_tf = (TextView) rootview.findViewById(R.id.doc_tf);
        date_tf = (TextView) rootview.findViewById(R.id.date_tf);
        symptom_tf = (TextView) rootview.findViewById(R.id.symptom_tf);
        content_tf = (TextView) rootview.findViewById(R.id.fb_tf);

        id = getArguments().getString("userID");
        doc = getArguments().getString("docName");
        date = getArguments().getString("fDate");
        nndate = getArguments().getString("fnnDate");
        symptom = getArguments().getString("fSymptom");
        content = getArguments().getString("fContent");

        doc_tf.setText("담당의사 : "+ doc);
        date_tf.setText(date);
        symptom_tf.setText(symptom);
        content_tf.setText(content);

        tentry = new ArrayList<>();
        hentry = new ArrayList<>();
        rmentry = new ArrayList<>();
        lmentry = new ArrayList<>();

        aes256Chiper = new AES256Chiper();

        lineChart = (LineChart) rootview.findViewById(R.id.datachart);//layout의 id
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

        String d = nndate.split(" ")[1];
        int h = Integer.parseInt(d.split(":")[0]);
        int m = Integer.parseInt(d.split(":")[1]);

        int hr1 = h, hr2 = h;
        int mr1 = m-3;
        int mr2 = m+4;
        if(mr1 < 0){
            mr1 = 60 + (mr1);
            hr1--;
            if(hr1 < 0){
                hr1 = 24 + (hr1); // 시간이 0에서 마이너스 될 수도
            }
        }
        if(mr2 >= 60){
            mr2 = mr2 - 60;
            hr2++;
            if(hr2 > 23){ //24시는 0시
                hr2 = 0;
            }
        }
        String fm = Integer.toString(mr1);
        String tm = Integer.toString(mr2);
        String fh = Integer.toString(hr1);
        String th = Integer.toString(hr2);

        if(mr1 < 10){
            fm = "0"+fm;
        }
        if(mr2 < 10){
            tm = "0"+tm;
        }
        if(hr1 < 10){
            fh = "0"+fh;
        }
        if(hr2 < 10){
            th = "0"+th;
        }

        ftime = fh +":"+fm;
        ttime = th +":"+tm;

        date = date.split(" ")[0];

        try {
            String e_id = aes256Chiper.encoding(id, MainActivity.key);
            String e_date = date;
            String e_ftime = ftime;
            String e_ttime =ttime;

            DataRequest nndatarequest_h = new DataRequest(3,1,e_id, e_date, e_ftime, e_ttime, nnListener);
            DataRequest nndatarequest_t = new DataRequest(2,e_id, e_date, e_ftime, e_ttime, nnListener);
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(nndatarequest_h);
            queue.add(nndatarequest_t);
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





        return rootview;
    }


    Response.Listener<String> nnListener = new Response.Listener<String>() { // 그래프 가져오는 리스너
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onResponse(String response) {
            if (response != null)
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    int i = 0;
                    int r = jsonArray.length();
                    time = new String[r];
                    format = new String[r];
                    String t = "";
                    String sensor = "";
                    while (i < r) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            String tmpt = jsonObject.getString("sTime");
                            time[i] = tmpt;
                            String t2 = tmpt.split(":")[0] + ":" + tmpt.split(":")[1];
                            if (!t.equals(t2)) {
                                format[i] = t2;
                                t = t2;
                         }
                            else{
                                format[i]="";
                            }
                            String ss = jsonObject.getString("sensor");
                            if (ss.equals("temp")) {
                                String val = aes256Chiper.decoding(jsonObject.getString("tValue"),MainActivity.key);
                                tentry.add(new Entry(i, Float.parseFloat(val)));
                                tc = true;
                            }
                            else if (ss.equals("heart")) {
                                String val = aes256Chiper.decoding(jsonObject.getString("hValue"), MainActivity.key);
                                hentry.add(new Entry(i,Integer.parseInt(val)));
                                hc = true;
                            }

                        }
                        i++;
                    }
                    Log.v("포맷",i+"");
                    xAxis.setLabelCount(i, true);
                    xAxis.setValueFormatter(new MyValueFormatter(format));

            if(hentry != null && hc == true){
                hline = new LineDataSet(hentry, "심박");
                init_linechart(hline, "#b377ac");
                hc = false;
            }
            if (tentry != null && tc == true) {
                tline = new LineDataSet(tentry, "체온");
                init_linechart(tline,"#b37e77");
                tc = false;

            }
            if (rmentry != null && lmentry != null && mc == true){
                rmline = new LineDataSet(rmentry, "근전도R");
                lmline = new LineDataSet(lmentry, "근전도L");
                init_linechart(rmline,"#778eb3");
                init_linechart(lmline, "#77b39c");
                mc = false;

            }

            MyMarkerView marker = new MyMarkerView(getContext(), R.layout.markerviewtext);
            marker.setChartView(lineChart);

            description = new Description();
            description.setText("");

            lineChart.setDoubleTapToZoomEnabled(false);
            lineChart.setDrawGridBackground(false);
            lineChart.setDescription(description);
            lineChart.setMarker(marker);
            lineChart.setData(chartData);
            lineChart.invalidate();

            int k =0;
            for(int j =0; j< format.length; j++){
                if(!("".equals(format[j]))){
                    k++;
                }
            }

            Log.v("포맷", "전체갯수 : "+format.length);
            Log.v("포맷","공백아닌거 : "+k);

    }catch (JSONException jsonException) {
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

    public void init_linechart(LineDataSet lineDataSet, String color){
        lineDataSet.setCircleColor(Color.parseColor(color));
        lineDataSet.setCircleColorHole(Color.BLUE);
        lineDataSet.setColor(Color.parseColor(color));
        chartData.addDataSet(lineDataSet);

    }

    public class MyValueFormatter implements IAxisValueFormatter {
        private String[] mValues;
        // 생성자 초기화
        public MyValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            System.out.println("밸류포매터 :" + value);

            return mValues[(int) value];
        }
    }

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