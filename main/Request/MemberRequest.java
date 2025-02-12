package com.example.main.Request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class MemberRequest extends StringRequest {
    final static private String IP = "192.168.0.105";
    final static private String URL_register = "http://"+IP+"/register.php";
    final static private String URL_login = "http://"+IP+"/app_login.php";
    final static private String URL_modify = "http://"+IP+"/modify.php";
    final static private String URL_modify1 = "http://"+IP+"/modifybtn.php";
    final static private String URL_modify2 = "http://"+IP+"/modifybtn2.php";
    final static private String URL_findid = "http:/"+IP+"/find_id.php";
    final static private String URL_findpw = "http://"+IP+"/find_pw.php";
    final static private String URL_checkid = "http://"+IP+"/checkid.php";
    final static private String URL_condoc = "http://"+IP+"/condoc.php";

    private Map<String, String> map;

    public MemberRequest(String id, String pw, String name, String gender, String birth
                           , String phone, String email, String address1, String address2, String blood
                           , String oper, String dise, String medi, Response.Listener<String> listener){
        super(Method.POST,URL_register,listener,null);

        map = new HashMap<>();
        System.out.println("맵값 : "+id+pw+name+gender+birth+phone+email+address1+address2+blood) ;
        map.put("userID",id);
        map.put("userPW",pw);
        map.put("userName",name);
        map.put("userGender",gender);
        map.put("userBirth",birth);
        map.put("userPhone",phone);
        map.put("userEmail",email);
        map.put("userAddress1",address1);
        map.put("userAddress2",address2);
        map.put("userBlood",blood);
        map.put("userOp",oper);
        map.put("userFd",dise);
        map.put("userTm",medi);
        System.out.println("값들 : "+map.get("userID"));
        System.out.println("정보 암호화 : "+id+" : "+pw+" : "+name+" : "+email+" : "+oper+" : "+medi);

    }

    public MemberRequest(String id,  String pw, Response.Listener<String> listener){
        super(Method.POST, URL_login, listener, null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("userPW",pw);

    }


    public MemberRequest(int main, String id, Response.Listener<String> listener){
        super(Method.POST,URL_modify,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
    }

    public MemberRequest( String id, String name, String pw, String address,
                          String address2, String phone, String email, String doc,
                          Response.Listener<String> listener){
        super(Method.POST,URL_modify1,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("userName",name);
        map.put("userPW",pw);
        map.put("userAddress1",address);
        map.put("userAddress2",address2);
        map.put("userPhone",phone);
        map.put("userEmail",email);
        map.put("conDoc",doc);

    }

    public MemberRequest( String id, String op, String fd, String tm, Response.Listener<String> listener){
        super(Method.POST,URL_modify2,listener,null);
        map = new HashMap<>();
        map.put("userID",id);
        map.put("userOp",op);
        map.put("userFd",fd);
        map.put("userTm",tm);
    }


    public MemberRequest( String name,String birth, String phone, Response.Listener<String> listener){
        super(Method.POST,URL_findid,listener,null);
        System.out.println("dddd:"+name+birth+phone);
        map = new HashMap<>();
        map.put("userName",name);
        map.put("userBirth",birth);
        map.put("userPhone",phone);
    }

    public MemberRequest(int findpw, String name, String userID, String Email,String randomCode, Response.Listener<String> listener){
        super(Method.POST,URL_findpw,listener,null);

        System.out.println("dddd:"+name+userID+Email);
        map = new HashMap<>();
        map.put("userName",name);
        map.put("userID",userID);
        map.put("userEmail",Email);
        map.put("randomCode",randomCode);
    }

    public MemberRequest(String id, Response.Listener<String> listener){
        super(Method.POST,URL_checkid,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
    }

    public MemberRequest(int doc, String id, String code, Response.Listener<String> listener){
        super(Method.POST,URL_condoc,listener,null);

        map = new HashMap<>();
        map.put("userID",id);
        map.put("conCode",code);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        System.out.println("리퀘스트 요청 성공 ");
        return map;
    }
}
