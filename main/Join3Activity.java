package com.example.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.MemberRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Join3Activity extends AppCompatActivity {

    private Toolbar toolbar;
    private RadioGroup radioGroup;
    private String tmp1, tmp2;
    private Button join3_btn;
    private EditText op_tf;
    private EditText fd_tf;
    private EditText tm_tf;
    private String blood="";
    private String op, fd, tm;
    private String id, pw, name, gender, birth, phone, email, address1, address2;
    private AES256Chiper aes256Chiper;

    private String key = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join3);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //디폴트(기본) 제목 삭제해줌
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼

        aes256Chiper = new AES256Chiper();

        radioGroup = (RadioGroup) findViewById(R.id.rh);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radio_btn = (RadioButton) findViewById(checkedId);
                tmp1 = radio_btn.getText().toString();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.abo);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                tmp2 = radioButton.getText().toString();
            }
        });

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean success = jsonObject.getBoolean("success");
                    if(success){
                        Toast.makeText(getApplicationContext(),"회원가입에 성공했습니다.\n환영합니다.",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };


        op_tf = (EditText) findViewById(R.id.operation_tf);
        fd_tf = (EditText) findViewById(R.id.disease_tf);
        tm_tf = (EditText) findViewById(R.id.medicine_tf);


        join3_btn = (Button) findViewById(R.id.join3_btn);
        join3_btn.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                System.out.println("tmp1 : "+ tmp1+ " : "+"tmp2 : "+tmp2);
                op = op_tf.getText().toString() + " "; //" "는 공백 방지
                fd = fd_tf.getText().toString() + " ";
                tm = tm_tf.getText().toString() + " ";
                blood = tmp1 +" "+ tmp2;

                if(null == tmp1){
                    Toast.makeText(getApplicationContext(),"RH형을 선택해 주세요.",Toast.LENGTH_SHORT).show();
                }
                else if(null == tmp2){
                    Toast.makeText(getApplicationContext(),"ABO형을 선택해 주세요.",Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent thisIntent = getIntent();
                    key = "qwertyuiopasdfghjklzxcvbnmqwerty";
                    try {
                        id = aes256Chiper.encoding(thisIntent.getStringExtra("memID"),key);
                        pw = thisIntent.getStringExtra("memPW");
                        name = aes256Chiper.encoding(thisIntent.getStringExtra("memName"),key);
                        gender = aes256Chiper.encoding(thisIntent.getStringExtra("memGender"),key);
                        birth = aes256Chiper.encoding(thisIntent.getStringExtra("memBirth"),key);
                        phone = aes256Chiper.encoding(thisIntent.getStringExtra("memPhone"),key);
                        email = aes256Chiper.encoding(thisIntent.getStringExtra("memEmail"),key);
                        address1 = aes256Chiper.encoding(thisIntent.getStringExtra("memAdr1"),key);
                        address2 = aes256Chiper.encoding(thisIntent.getStringExtra("memAdr2"),key);
                        blood = aes256Chiper.encoding(blood,key);
                        if(op != " "){
                            op = aes256Chiper.encoding(op,key);
                        }
                        if(fd != " "){
                            fd = aes256Chiper.encoding(fd,key);
                        }
                        if(tm != " "){
                            tm = aes256Chiper.encoding(tm, key);
                        }

                        MemberRequest registerRequest = new MemberRequest(id, pw, name, gender,
                                birth, phone, email, address1,address2, blood, op, fd, tm, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(Join3Activity.this);
                        queue.add(registerRequest);

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
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}