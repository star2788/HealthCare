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
import com.example.main.utils.Encryption;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class JoinActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private Button join1_btn;
    private Button checkid_btn;
    private EditText id_tf;
    private EditText pw_tf;
    private EditText pw1_tf;
    private EditText name_tf;
    private RadioGroup gender_g;
    private String id;
    private String pw1;
    private String pw2;
    private String name;
    private String gender;
    private boolean check = false;
    private AES256Chiper aes256Chiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join1);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //디폴트(기본) 제목 삭제해
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼

        id_tf = (EditText)findViewById(R.id.memid_tf);
        pw1_tf = (EditText)findViewById(R.id.mempw1_tf);
        pw_tf = (EditText)findViewById(R.id.mempw2_tf); //비밀번호 확인
        name_tf = (EditText)findViewById(R.id.memnm_tf);
        gender_g = (RadioGroup) findViewById(R.id.memgener_g);

        aes256Chiper = new AES256Chiper();


        checkid_btn = (Button) findViewById(R.id.idcheck_btn);
        checkid_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String id = id_tf.getText().toString();


                if("".equals(id)){
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요",Toast.LENGTH_SHORT).show();
                    id_tf.setSelection(0);
                }
                else{
                    System.out.println("\n"+id+"\n");
                    Response.Listener<String> responseListener = new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                boolean success = jsonObject.getBoolean("success");
                                System.out.println("success : "+success);
                                if(success){
                                    Toast.makeText(getApplicationContext(),"사용 가능 아이디입니다.",Toast.LENGTH_LONG).show();
                                    check = true;
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"사용할 수 없는 아이디입니다.",Toast.LENGTH_LONG).show();
                                    id_tf.setText("");
                                    id_tf.setSelection(id_tf.length());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    };

                    try {
                        String e_id = aes256Chiper.encoding(id,MainActivity.key);
                        MemberRequest checkIdRequest = new MemberRequest(e_id, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(JoinActivity.this);
                        queue.add(checkIdRequest);
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

        gender_g = (RadioGroup) findViewById(R.id.memgener_g);
        gender_g.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                gender = radioButton.getText().toString();
            }
        });

        join1_btn = (Button) findViewById(R.id.join1_btn);
        join1_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                id = id_tf.getText().toString();
                pw1 = Encryption.SHA256(pw1_tf.getText().toString());
                pw2 = Encryption.SHA256(pw_tf.getText().toString());
                name = name_tf.getText().toString();



                if(check == false){
                    Toast.makeText(getApplicationContext(),"아이디 중복확인이 필요합니다.",Toast.LENGTH_LONG).show();
                }
                else if("".equals(id)){
                    Toast.makeText(getApplicationContext(),"아이디를 입력해주세요.",Toast.LENGTH_LONG).show();
                    id_tf.setSelection(id_tf.length());
                }
                else if("".equals(pw1)){
                    Toast.makeText(getApplicationContext(),"비밀번호를 입력해주세요.",Toast.LENGTH_LONG).show();
                    pw1_tf.setSelection(pw1_tf.length());
                }
                else if("".equals(pw2)){
                    Toast.makeText(getApplicationContext(),"비밀번호 확인을 입력해주세요",Toast.LENGTH_LONG).show();
                    pw_tf.setSelection(pw_tf.length());
                }
                else if("".equals(name)){
                    Toast.makeText(getApplicationContext(),"이름을 입력해 주세요.",Toast.LENGTH_LONG).show();
                    name_tf.setSelection(name_tf.length());
                }
                else if(!(pw1.equals(pw2))){
                    Toast.makeText(getApplicationContext(),"비밀번호가 일치하지 않습니다.",Toast.LENGTH_LONG).show();
                    pw_tf.setSelection(pw_tf.length());
                }
                else{
                    Intent intent = new Intent(getApplicationContext(),Join2Activity.class);
                    intent.putExtra("memID", id);
                    intent.putExtra("memPW",pw2);
                    intent.putExtra("memName",name);
                    intent.putExtra("memGender",gender);
                    startActivity(intent);
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