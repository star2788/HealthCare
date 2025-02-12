package com.example.main.Request;

import android.speech.RecognitionListener;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RecognitionRequest extends StringRequest {
    final static private String IP = "192.168.0.105";
    final static private String URL_rr = "http://"+IP+"/ConReser.php"; //예약확인
    final static private String URL_chtime = "http://"+IP+"/calendarbtn.php"; //예약가능시간확인
    final static private String URL_res = "http://"+IP+"/reservation.php"; //병원예약
    final static private String URL_rdfb = "http://"+IP+"/readfb.php"; // 피드백 읽기
    final static private String URL_sendm = "http://"+IP+"/chat_send.php"; //메세지 보내기
    final static private String URL_recm = "http://"+IP+"/chat_receive.php"; //최근 수신 메세지 읽기


    private Map<String, String> map;

    public RecognitionRequest(String id, Response.Listener<String> listener) { //병원예약확인
        super(Method.POST,URL_rr, listener, null);
        map = new HashMap<>();
        map.put("userID",id);
        System.out.println("");
    }
    public RecognitionRequest(String id, String date, Response.Listener<String> listener){ //예약가능시간확인
        super(Method.POST, URL_chtime, listener, null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("resDate",date);
    }
    public RecognitionRequest(String id, String date, String time, String doc, Response.Listener<String> listener){ //병원예약
        super(Method.POST, URL_res, listener, null);
        map = new HashMap<>();
        map.put("userID", id);
        map.put("resTime",time);
        map.put("resDate",date);
        map.put("encoDID",doc);
    }
    public RecognitionRequest(int k, String id, String date, Response.Listener<String>listener){ //특정날짜피드백읽기
        super(Method.POST, URL_rdfb,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("Date",date);
        System.out.println("피드백 날짜 :"+id + " : "+date);

    }
    public RecognitionRequest(int k, String id, Response.Listener<String>listener){ // 제일 최근 피드백 읽기
        super(Method.POST, URL_rdfb, listener, null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("Date","aa");

    }
    public RecognitionRequest(int k,String msg, String date, String id, String receiver, String send, String chk,  Response.Listener<String>listener){
        super(Method.POST,URL_sendm,listener,null);
        map = new HashMap<>();
        map.put("msg",msg);
        map.put("date",date);
        map.put("sender",id);
        map.put("receiver",receiver);
        map.put("send",send);
        map.put("chk",chk);


    }
    public RecognitionRequest(int k , int i, String id, Response.Listener<String>listener){
        super(Method.POST, URL_recm, listener, null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("number","");
    }


    @Override
    protected Map<String, String> getParams(){
        System.out.println("요청3");
        return map;
    }
}
