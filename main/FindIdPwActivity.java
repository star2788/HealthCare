package com.example.main;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.MemberRequest;
import com.example.main.utils.Encryption;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class FindIdPwActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button find_pw_btn;
    private TextView foundid_tf;
    private Button find_id_btn;
    private EditText find_pw_name,find_pw_id,find_pw_email;
    private EditText name_tf, birth_tf, phone_tf;
    private String name, birth, phone;
    private String pw_name, pw_id,pw_email, RandomCode;
    boolean success = false;
    private GMailSender gMailSender = new GMailSender("shm103187@gmail.com", "dmsel10704");
    private AES256Chiper aes256Chiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_idpw);

        aes256Chiper = new AES256Chiper();

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitDiskReads().permitDiskWrites().permitNetwork().build());
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //디폴트(기본) 제목 삭제해줌
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼;


        find_pw_name = findViewById(R.id.f_pw_name);
        find_pw_id = findViewById(R.id.f_pw_id);
        find_pw_email = findViewById(R.id.f_pw_email);
        find_pw_btn = findViewById(R.id.findpw_btn);
        find_id_btn = findViewById(R.id.findid_btn);


        RandomCode = gMailSender.getEmailCode().toUpperCase();

        find_pw_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try{
                    pw_name = find_pw_name.getText().toString();
                    pw_id = find_pw_id.getText().toString();
                    pw_email = find_pw_email.getText().toString();


                    Response.Listener<String> responseListener_pw = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                success = jsonObject.getBoolean("success");

                                String msg = "아래 비밀번호로 로그인 후, 반드시 비밀번호를 변경하세요.\n"+ Encryption.SHA256(RandomCode);

                                if(success) {
                                    gMailSender.sendMail("HealthCare 비밀번호 변경 안내", msg, find_pw_email.getText().toString());
                                    Toast.makeText(getApplicationContext(), "이메일을 성공적으로 보냈습니다.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"해당 정보가 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    MemberRequest findPwRequest = new MemberRequest(3, pw_name, pw_id, pw_email,RandomCode, responseListener_pw);
                    RequestQueue queue = Volley.newRequestQueue(FindIdPwActivity.this);
                    queue.add(findPwRequest);


                    // Gmailsender 만들고 랜덤코드 생성 후 버튼누를시에 랜덤코드를 php로 발송해서 성공시에 update문 같이 실행행

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_view);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                changeView(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        Button login_btn = (Button) findViewById(R.id.idtologin_btn);
        Button find_id_btn = (Button) findViewById(R.id.findid_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        name_tf = findViewById(R.id.f_id_name);
        birth_tf = findViewById(R.id.f_id_birth);
        phone_tf = findViewById(R.id.f_id_phone);
        foundid_tf = findViewById(R.id.f_id_found);

        find_id_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                name = name_tf.getText().toString();
                birth = birth_tf.getText().toString();
                phone = phone_tf.getText().toString();


                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if(success){
                                String id = aes256Chiper.decoding(jsonObject.getString("userID"),MainActivity.key);
                                StringBuffer sb = new StringBuffer();
                                sb.append(id);
                                sb.replace(1,3,"***");
                                foundid_tf.setText(sb);
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"해당 정보가 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                };

                try {
                    String e_name = aes256Chiper.encoding(name,MainActivity.key);
                    String e_birth = aes256Chiper.encoding(birth,MainActivity.key);
                    String e_phone = aes256Chiper.encoding(phone,MainActivity.key);

                    MemberRequest findIdRequest = new MemberRequest(e_name, e_birth, e_phone, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(FindIdPwActivity.this);
                    queue.add(findIdRequest);

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
    }

    private void changeView(int index){
        ConstraintLayout f_id_layout = (ConstraintLayout) findViewById(R.id.findid);
        ConstraintLayout f_pw_layout = (ConstraintLayout) findViewById(R.id.findpw);

        switch(index){
            case 0:
                f_id_layout.setVisibility(View.VISIBLE);
                f_pw_layout.setVisibility(View.INVISIBLE);
                break;
            case 1:
                f_id_layout.setVisibility(View.INVISIBLE);
                f_pw_layout.setVisibility(View.VISIBLE);
                break;
        }
    }
}