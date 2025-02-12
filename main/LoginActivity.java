package com.example.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.MemberRequest;
import com.example.main.utils.Encryption;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class LoginActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private EditText id_tf, pw_tf;
    private Button login_btn;
    private int y, m, d, h, mn;
    private CheckBox checkBox;
    private Boolean loginvalidation;
    private Boolean loginChecked = false;
    private String key = "qwertyuiopasdfghjklzxcvbnmqwerty";

    private AES256Chiper aes256Chiper;





    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        checkBox = findViewById(R.id.checkBox2);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //디폴트(기본) 제목 삭제해줌
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼

        TextView findidpw = (TextView) findViewById(R.id.name_tf);
        login_btn = (Button) findViewById(R.id.login_btn);
        id_tf = (EditText) findViewById(R.id.id_tf);
        pw_tf = (EditText) findViewById(R.id.pw_tf);

        aes256Chiper = new AES256Chiper();

        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = auto.edit();

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    loginChecked = true;
                }
                else{
                    loginChecked = false;
                    editor.clear();
                    editor.commit();
                }
            }
        });

        if(auto.getBoolean("autoLogin",false)){
            checkBox.setChecked(true);
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("userName", auto.getString("userName",""));
            intent.putExtra("userBlood", auto.getString("userBlood",""));
            intent.putExtra("userBirth", auto.getString("userBirth",""));
            intent.putExtra("userID", auto.getString("userID",""));
            intent.putExtra("userPW", auto.getString("userPW",""));
            intent.putExtra("userMBL", auto.getString("TEST",""));
            intent.putExtra("conDoc", auto.getString("conDoc",""));
            intent.putExtra("docName", auto.getString("docName",""));
            Toast.makeText(getApplicationContext(), "자동 로그인되었습니다.", Toast.LENGTH_SHORT).show();

            startActivity(intent);
        }
        else {

            login_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userID = id_tf.getText().toString();
                    String userPW = Encryption.SHA256(pw_tf.getText().toString());
                    System.out.println("해싱암호화 : "+userPW);
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                loginvalidation = jsonObject.getBoolean("success");
                                if (loginvalidation) {//로그인에 성공한 경우

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    System.out.println("ㅎㅘㄱ인인비이ㅣ이잉ㅇ");
                                    String userID = aes256Chiper.decoding(jsonObject.getString("userID"),key);
                                    String userName = aes256Chiper.decoding(jsonObject.getString("userName"),key);
                                    String userBlood = aes256Chiper.decoding(jsonObject.getString("userBlood"),key);
                                    String userBirth = aes256Chiper.decoding(jsonObject.getString("userBirth"),key);
                                    String name = jsonObject.getString("docName");

                                    System.out.println("아이디 암호화 : "+jsonObject.getString("userID"));
                                    String userPW = jsonObject.getString("userPW");
                                    String userDoc = null, docName;

                                    if(!(jsonObject.getString("conDoc")).equals("null")){
                                        userDoc = aes256Chiper.decoding(jsonObject.getString("conDoc"),key);
                                        docName = aes256Chiper.decoding(jsonObject.getString("docName"),key);
                                    }
                                    else{
                                        userDoc = jsonObject.getString("conDoc");
                                        docName = "";
                                    }
                                    intent.putExtra("userName", userName);
                                    intent.putExtra("userBlood", userBlood);
                                    intent.putExtra("userBirth", userBirth);
                                    intent.putExtra("userID", userID);
                                    intent.putExtra("userPW", userPW);
                                    intent.putExtra("conDoc", userDoc);
                                    intent.putExtra("docName", docName);
                                    if (loginChecked) {
                                        editor.putString("userID", userID);
                                        editor.putString("userPW", userPW);
                                        editor.putBoolean("autoLogin", true);
                                        editor.putString("userBlood", userBlood);
                                        editor.putString("userBirth", userBirth);
                                        editor.putString("userName", userName);
                                        editor.putString("conDoc",userDoc);
                                        editor.putString("docName",docName);
                                        editor.commit();

                                    }

                                    Toast.makeText(getApplicationContext(), "로그인되었습니다.", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplicationContext(), "로그인 실패하였습니다..", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
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
                    try {
                        String id = aes256Chiper.encoding(userID,key);

                        MemberRequest loginRequest = new MemberRequest(id, userPW, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                        queue.add(loginRequest);

                        System.out.println("로그인 테스트 " +id+"  /  "+userPW);
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
            findidpw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), FindIdPwActivity.class);
                    startActivity(intent);
                }
            });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setRecentlyRes(Intent intent, String r_time, String r_date, String d_name, String d_work) {
        intent.putExtra("r_time", r_time);
        intent.putExtra("r_date", r_date);
        intent.putExtra("d_name", d_name);
        intent.putExtra("d_work", d_work);
    }
}


