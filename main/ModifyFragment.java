package com.example.main;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.MemberRequest;
import com.example.main.utils.Encryption;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class ModifyFragment extends Fragment {

    private Button basic_btn;
    private Button medical_btn;
    private EditText id_tf, name_tf, pw1_tf, pw2_tf, cc_tf,
            birth_tf, address_tf, address_tf2, phone_tf, email_tf, op_tf, fd_tf, tm_tf;
    private RadioButton a, b, o, ab, rhp, rhm, male, female;
    private Context ct;
    private String PW;
    private Handler handler;
    private WebView daum_webView;
    private View rootView;
    private AES256Chiper aes256Chiper;
    private String blood, age, name, userid;
    private Frag_main frag_main;

    public static ModifyFragment newInstance() { return new ModifyFragment(); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_modify, container, false);
        ct = container.getContext();

        id_tf = rootView.findViewById(R.id.userid);
        name_tf = rootView.findViewById(R.id.username);
        pw1_tf = rootView.findViewById(R.id.userpw);
        pw2_tf = rootView.findViewById(R.id.userpwc);
        cc_tf = rootView.findViewById(R.id.concode);
        birth_tf = rootView.findViewById(R.id.userbd);
        address_tf = rootView.findViewById(R.id.useradr);
        address_tf2 = rootView.findViewById(R.id.useradr2);
        phone_tf = rootView.findViewById(R.id.userphone);
        email_tf = rootView.findViewById(R.id.useremail);
        op_tf = rootView.findViewById(R.id.userop);
        fd_tf = rootView.findViewById(R.id.userfd);
        tm_tf = rootView.findViewById(R.id.usertm);
        a = rootView.findViewById(R.id.a_r);
        b = rootView.findViewById(R.id.b_r);
        o = rootView.findViewById(R.id.o_r);
        ab = rootView.findViewById(R.id.ab_r);
        rhp = rootView.findViewById(R.id.rhm_r);
        rhm = rootView.findViewById(R.id.rhp_r);
        male = rootView.findViewById(R.id.male_rb);
        female = rootView.findViewById(R.id.female_rb);

        aes256Chiper = new AES256Chiper();
        frag_main = new Frag_main();

        setData_basic();

        init_webView();
        handler = new Handler();

        TabLayout tabLayout = (TabLayout)rootView.findViewById(R.id.modify_tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                changeView(pos,rootView);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        address_tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daum_webView = rootView.findViewById(R.id.adrweb);
                daum_webView.setVisibility(View.VISIBLE);
            }
        });


        basic_btn = rootView.findViewById(R.id.basic_btn);
        medical_btn = rootView.findViewById(R.id.medical_btn);

        basic_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                userid = id_tf.getText().toString();
                name = name_tf.getText().toString();
                String address = address_tf.getText().toString();
                String address2 = address_tf2.getText().toString();
                String phone = phone_tf.getText().toString();
                String email = email_tf.getText().toString();
                String doc = cc_tf.getText().toString();

                String pw;
                String tmp = checkPW();
                if(!(tmp.equals("no"))){
                    pw = tmp;
                }
                else{
                    pw = PW;
                }

                String abo = blood.split(" ")[1];

                System.out.println("정보 : "+userid+" "+name+" "+address+" "+address2+" "+phone+" "+email+" "+pw);
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){//로그인에 성공한 경우
                                Toast.makeText(ct, "정보가 수정되었습니다.",Toast.LENGTH_SHORT).show();

                                Bundle bundle = new Bundle();

                                bundle.putString("Name", name);
                                bundle.putString("Blood",abo);
                                bundle.putString("Age", age);
                                bundle.putString("userID",userid);
                                frag_main.setArguments(bundle);

                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_main).commit();





                                setData_basic();
                            }
                            else{
                                Toast.makeText(ct, "정보수정에 실패하였습니다..",Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                };
                try {
                    String e_id = aes256Chiper.encoding(userid,MainActivity.key);
                    String e_name = aes256Chiper.encoding(name,MainActivity.key);
                    String e_pw = Encryption.SHA256(pw);
                    String e_ar = aes256Chiper.encoding(address,MainActivity.key);
                    String e_ar2 = aes256Chiper.encoding(address2,MainActivity.key);
                    String e_phone = aes256Chiper.encoding(phone, MainActivity.key);
                    String e_email = aes256Chiper.encoding(email, MainActivity.key);
                    String e_doc = aes256Chiper.encoding(doc,MainActivity.key);

                    MemberRequest modifyBtnRequest= new MemberRequest(e_id, e_name, e_pw, e_ar, e_ar2, e_phone, e_email, e_doc,responseListener);
                    RequestQueue queue = Volley.newRequestQueue(ct);
                    queue.add(modifyBtnRequest);
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

        medical_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String id = id_tf.getText().toString();
                String op = op_tf.getText().toString()+" ";//공백 방지
                String fd = fd_tf.getText().toString()+" ";
                String tm = tm_tf.getText().toString()+" ";
                String abo = blood.split(" ")[1];


                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){
                                Toast.makeText(ct, "정보가 수정되었습니다.",Toast.LENGTH_SHORT).show();

                                Bundle bundle = new Bundle();

                                bundle.putString("Name", name);
                                bundle.putString("Blood",abo);
                                bundle.putString("Age", age);
                                bundle.putString("userID",userid);
                                frag_main.setArguments(bundle);

                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, frag_main).commit();

                            }
                            else{
                                Toast.makeText(ct, "정보수정에 실패하였습니다..",Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                };

                try {
                    String e_id = aes256Chiper.encoding(id, MainActivity.key);
                    String e_op = aes256Chiper.encoding(op, MainActivity.key);
                    String e_fd = aes256Chiper.encoding(fd, MainActivity.key);
                    String e_tm = aes256Chiper.encoding(tm, MainActivity.key);

                    MemberRequest modifyBtnRequest= new MemberRequest(id, op, fd, tm, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(ct);
                    queue.add(modifyBtnRequest);


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

        return rootView;
    }
    private void changeView(int index, View rootView){
        ScrollView modify_basic = (ScrollView) rootView.findViewById(R.id.basic_m);
        ScrollView modify_medical = (ScrollView) rootView.findViewById(R.id.medical_m);

        switch(index){
            case 0:
                modify_basic.setVisibility(View.VISIBLE);
                modify_medical.setVisibility(View.INVISIBLE);
                break;
            case 1:
                modify_basic.setVisibility(View.INVISIBLE);
                modify_medical.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setData_basic(){
        id_tf.setText(getArguments().getString("ID"));
        name_tf.setText(getArguments().getString("Name"));
        birth_tf.setText(getArguments().getString("Birth"));
        address_tf.setText(getArguments().getString("Address1"));
        address_tf2.setText(getArguments().getString("Address2"));
        phone_tf.setText(getArguments().getString("Phone"));
        email_tf.setText(getArguments().getString("Email"));
        op_tf.setText(getArguments().getString("Op"));
        fd_tf.setText(getArguments().getString("Fd"));
        tm_tf.setText(getArguments().getString("Tm"));
        PW = getArguments().getString("PW");
        age = getArguments().getString("Age");

        if(!"".equals(getArguments().getString("conDoc"))){
            cc_tf.setText(getArguments().getString("conDoc"));
        }

        id_tf.setEnabled(false);
        birth_tf.setEnabled(false);
        male.setEnabled(false);
        female.setEnabled(false);

        String gender = getArguments().getString("Gender");
        if("남".contains(gender)){
            male.setChecked(true);
        }
        else if("여".contains(gender)){
            female.setChecked(true);
        }


        blood = getArguments().getString("Blood");
        String rh = blood.split(" ")[0];
        String abo = blood.split(" ")[1];
        System.out.println("혈액형 : "+rh +abo);

        if("RH+".equals(rh)){
            rhp.setChecked(true);
        }
        else if("RH-".equals(rh)){
            rhm.setChecked(true);
        }

        setBlood(abo);
    }

    private void setBlood(String blood){
        if("A".equals(blood)){
            a.setChecked(true);
        }
        else if("B".equals(blood)){
            b.setChecked(true);
        }
        else if("O".equals(blood)){
            o.setChecked(true);
        }
        else if("AB".equals(blood)){
            ab.setChecked(true);
        }
        a.setEnabled(false);
        b.setEnabled(false);
        o.setEnabled(false);
        ab.setEnabled(false);
        rhp.setEnabled(false);
        rhm.setEnabled(false);
    }
    public String checkPW() {
        String pw1 = pw1_tf.getText().toString();
        String pw2 = pw2_tf.getText().toString();
        String tmp = "";

        if ("".equals(pw1)) { //비밀번호 변경을 안할 경우
            tmp = "no";
        }
        else {
            if ("".equals(pw2)) {
                Toast.makeText(ct, "비밀번호 확인란을 확인해주세요",Toast.LENGTH_SHORT).show();
            } else if (pw1.equals(pw2)) {
                tmp =  pw1;
            }
            else {
                Toast.makeText(ct, "비밀번호가 일치하지 않습니다.",Toast.LENGTH_SHORT).show();
                pw2_tf.setText("");
            }
        }

        return tmp;
    }

    public void init_webView() {
        daum_webView = (WebView) rootView.findViewById(R.id.adrweb);
        daum_webView.getSettings().setJavaScriptEnabled(true);
        daum_webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        daum_webView.addJavascriptInterface(new AndroidBridge2(), "TestApp");
        daum_webView.setWebChromeClient(new WebChromeClient());
        daum_webView.loadUrl("http://192.168.0.9/daum_address.php");
    }

    private class AndroidBridge2 {
        @JavascriptInterface
        public void setAddress(final String arg1, final String arg2, final String arg3) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    daum_webView = rootView.findViewById(R.id.adrweb);
                    daum_webView.setVisibility(View.INVISIBLE);
                    address_tf.setText(String.format("(%s) %s %s", arg1, arg2, arg3));

                    init_webView();
                }
            });
        }
    }

}