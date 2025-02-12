package com.example.main.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ChatRequest extends StringRequest {
    final static private String IP = "192.168.0.105";
    final static private String URL_send = "http://"+IP+"/chat_send.php";
    final static private String URL_receive = "http://"+IP+"/chat_receive.php";
    final static private String URL_recently = "http://"+IP+"/chat_receive.php";


    private Map<String, String> map;

    public ChatRequest(String msg, String date, String sender, String receiver, String send, String chk, Response.Listener<String> listener){
        super(Method.POST,URL_send,listener,null);
        map = new HashMap<>();
        map.put("msg",msg);
        map.put("date",date);
        map.put("sender",sender);
        map.put("receiver",receiver);
        map.put("send",send);
        map.put("chk",chk);

    }

    public ChatRequest(int number,String id, Response.Listener<String> listener){
        super(Method.POST,URL_receive,listener,null);
        map = new HashMap<>();
        map.put("number",String.valueOf(number));
        map.put("userID",id);
    }

    public ChatRequest(String id, Response.Listener<String> listener){
        super(Method.POST,URL_recently,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        System.out.println("리퀘스트 넘어감");

        return map;
    }
}
