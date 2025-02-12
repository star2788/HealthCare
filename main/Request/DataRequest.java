package com.example.main.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class DataRequest extends StringRequest {
    final static private String IP = "192.168.0.105";
    final static private String URL_T = "http://"+IP+"/tdataset.php";
    final static private String URL_Mnn = "http://"+IP+"/nnmdata.php";
    final static private String URL_Tnn = "http://"+IP+"/nntdata.php";
    final static private String URL_M = "http://"+IP+"/mdataset.php";
    final static private String URL_Hnn = "http://"+IP+"/nnhdata.php";
    final static private String URL_H = "http://"+IP+"/hdataset.php";




    private Map<String, String> map;

    public DataRequest(String id, String fdate, String tdate, Response.Listener<String> listener){
        super(Method.POST,URL_T,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",fdate);
        map.put("selDate2",tdate);
        map.put("selTime1","check");
        map.put("selTime2","check");
        System.out.println("값 : "+id+" "+fdate+" "+tdate);

    }
    public DataRequest(String id, String fdate, String tdate, String ftime, String ttime, Response.Listener<String> listener){
        super(Method.POST,URL_T,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",fdate);
        map.put("selDate2",tdate);
        map.put("selTime1",ftime);
        map.put("selTime2",ttime);
        System.out.println("값 : "+id+" "+fdate+" "+tdate+" "+ftime+" "+ttime);

    }

    public DataRequest(int Tnn,String id, String date, String t1, String t2, Response.Listener<String> listener){
        super(Method.POST,URL_Tnn,listener,null);
        System.out.println("체크3 : "+id+date+t1+t2);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("sDate",date);
        map.put("sTime1", t1);
        map.put("sTime2", t2);
    }
    public DataRequest(String id, String date, String t1, String t2, Response.Listener<String> listener){
        super(Method.POST,URL_Mnn,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("sDate",date);
        map.put("sTime1", t1);
        map.put("sTime2", t2);
    }

    public DataRequest(int m, String id, String fdate, String tdate, Response.Listener<String> listener){
        super(Method.POST,URL_M,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",fdate);
        map.put("selDate2",tdate);
        map.put("selTime1","check");
        map.put("selTime2","check");
        System.out.println("값 : "+id+" "+fdate+" "+tdate);

    }
    public DataRequest(int m, String id, String fdate, String tdate, String ftime, String ttime, Response.Listener<String> listener){
        super(Method.POST,URL_M,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",fdate);
        map.put("selDate2",tdate);
        map.put("selTime1",ftime);
        map.put("selTime2",ttime);
        System.out.println("값 : "+id+" "+fdate+" "+tdate+" "+ftime+" "+ttime);

    }

    public DataRequest(int h, int hh, String id, String date, String t1, String t2, Response.Listener<String> listener){
        super(Method.POST,URL_Hnn,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("sDate",date);
        map.put("sTime1", t1);
        map.put("sTime2", t2);
    }

    public DataRequest(char h, String id, String fdate, String tdate, Response.Listener<String> listener){
        super(Method.POST,URL_H,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",fdate);
        map.put("selDate2",tdate);
        map.put("selTime1","check");
        map.put("selTime2","check");
        System.out.println("값 : "+id+" "+fdate+" "+tdate);

    }
    public DataRequest(char h, int hh, String id, String fdate, String tdate, String ftime, String ttime, Response.Listener<String> listener){
        super(Method.POST,URL_H,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("selDate1",fdate);
        map.put("selDate2",tdate);
        map.put("selTime1",ftime);
        map.put("selTime2",ttime);
        System.out.println("값 : "+id+" "+fdate+" "+tdate+" "+ftime+" "+ttime);

    }


    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        System.out.println("값3"+map);
        return map;
    }
}
