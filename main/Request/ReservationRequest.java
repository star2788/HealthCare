package com.example.main.Request;

import android.icu.text.SimpleDateFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.google.protobuf.Method;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReservationRequest extends StringRequest {
    final static private String IP = "192.168.0.105";
    final static private String URL = "http://"+IP+"/reservation.php";
    final static private String URL_cancel = "http://"+IP+"/cancel_res.php";
    final static private String URL_confirm = "http://"+IP+"/ConReser.php";
    final static private String URL_recentlyres = "http://"+IP+"/recentlyres.php";
    final static private String URL_resbtn = "http://"+IP+"/calendarbtn.php";

    private Map<String, String> map;

    public ReservationRequest( String id, String time, String date, String doc_id, Response.Listener<String> listener){
        super(Method.POST,URL,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("resTime",time);
        map.put("resDate",date);
        map.put("encoDID",doc_id);

    } //reservation

    public ReservationRequest(String res_num, Response.Listener<String> listener){
        super(Method.POST,URL_cancel,listener,null);
        map = new HashMap<>();
        map.put("resNum",res_num);
    } //cancel

    public ReservationRequest(String confirm, String id,Response.Listener<String> listener){
        super(Method.POST,URL_confirm,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
        System.out.println("원라이~~~");
    } //confirm


    @RequiresApi(api = Build.VERSION_CODES.N)
    public ReservationRequest(int recentlyres, String id, Response.Listener<String> listener){
        super(Method.POST, URL_recentlyres, listener, null);
        map = new HashMap<>();
        map.put("userID",id);
        System.out.println("레저베이션 리센틀리 : "+id);

    } //recentlyres

    public ReservationRequest(int resbtn, String id,String date, Response.Listener<String> listener){
        super(Method.POST,URL_resbtn,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("resDate",date);
    } //resbtn




    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        System.out.println("맵 겟겟  : "+map.get("userID"));
        return map;
    }
}
