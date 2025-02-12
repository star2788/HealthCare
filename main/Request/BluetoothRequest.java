package com.example.main.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class BluetoothRequest extends StringRequest {

    final static private String IP = "192.168.0.105";
    final static private String URL1 = "http://"+IP+"/insert_data.php";
    final static private String URL2 = "http://"+IP+"/insert_data_sec.php";
    private Map<String, String> map;

    public BluetoothRequest(String id, String val,
                            String date, String time, String val2, Response.Listener<String> listener){
        super(Method.POST,URL2,listener,null);
        //이상값 1초간격 저장
        map = new HashMap<>();
        map.put("userID",id);
        map.put("Value",val+""); // 체온
        map.put("Date",date);
        map.put("Time",time);
        map.put("Value2",val2+""); // 심박
    }

    public BluetoothRequest(String id, String val, String val2, String val3, String val4, String date, String time, Response.Listener<String> listener){
        super(Method.POST,URL1,listener,null);
        //이상값 평균(3분간격) 저장
        map = new HashMap<>();
        map.put("userID",id);
        map.put("Value",val+""); // 체온
        map.put("Value2",val2+""); // 심박
        map.put("Value3",val3+""); //근전도우
        map.put("Value4",val4+""); //근전도좌
        map.put("Date",date);
        map.put("Time",time);
    }


    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}

