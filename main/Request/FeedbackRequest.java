package com.example.main.Request;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class FeedbackRequest extends StringRequest {
    final static private String IP = "192.168.0.105";
    final static private String URL = "http://"+IP+"/feedback.php";
    final static private String URL_alarm = "http://"+IP+"/fbrequest.php";

    private String m = "";
    private Map<String, String> map;

    public FeedbackRequest(String id,Response.Listener<String> listener){
        super(Method.POST,URL,listener,null);

        System.out.println("1번 아이디: " + id);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1","check");
        map.put("selDate2","check");

    }

    public FeedbackRequest(String id, String d1, String d2,Response.Listener<String> listener){
        super(Method.POST,URL,listener,null);

        System.out.println(id);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",d1+" 00:00");
        map.put("selDate2",d2+" 23:59");
    }

    public FeedbackRequest(String id, String d1, String d2,String t1, String t2,Response.Listener<String> listener){
        super(Method.POST,URL,listener,null);

        System.out.println(id);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",d1+" "+t1);
        map.put("selDate2",d2+" "+t2);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public FeedbackRequest(String id, String date, String symptom, String mode, String doc, String name, String neww ,Response.Listener<String> listener){
        super(Method.POST,URL_alarm,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("Date",date);
        map.put("Symptom", symptom);
        map.put("Mode", m);
        map.put("Name", name);
        map.put("New", neww);
        map.put("docID", doc);
    }
    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        System.out.println("맵 : "+map);
        return map;
    }
}
