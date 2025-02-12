package com.example.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.main.R;

public class Join2Activity extends AppCompatActivity {


    private Toolbar toolbar;
    private Button join2_btn;
    private String id, pw, name, gender, birth, phone, email1,email2, email, address1, address2;
    private EditText birth_tf, phone_tf, email1_tf, email2_tf, adr1_tf, adr2_tf;
    private Handler handler;
    private WebView daum_webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join2);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false); //디폴트(기본) 제목 삭제해줌
        actionBar.setDisplayHomeAsUpEnabled(true); //자동 뒤로가기 버튼;


        birth_tf = findViewById(R.id.birth_tf);
        phone_tf = findViewById(R.id.phone_tf);
        email1_tf = findViewById(R.id.email_tf);
        email2_tf = findViewById(R.id.email_tf2);
        adr1_tf = findViewById(R.id.address1_tf);
        adr2_tf = findViewById(R.id.address2_tf);

        init_webView();
        handler = new Handler();

        adr1_tf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daum_webView = findViewById(R.id.daum_webview);
                daum_webView.setVisibility(View.VISIBLE);
            }
        });

        Intent thisIntent = getIntent();
        id = thisIntent.getStringExtra("memID");
        pw = thisIntent.getStringExtra("memPW");
        name = thisIntent.getStringExtra("memName");
        gender = thisIntent.getStringExtra("memGender");


        join2_btn = (Button) findViewById(R.id.join2_btn);
        join2_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                birth = birth_tf.getText().toString();
                phone = phone_tf.getText().toString();
                email1 = email1_tf.getText().toString();
                email2 = email2_tf.getText().toString();
                email = email1 + email2;
                address1 = adr1_tf.getText().toString();
                address2 = adr2_tf.getText().toString();

                if("".equals(birth)){
                    Toast.makeText(getApplicationContext(),"생일을 입력하세요.",Toast.LENGTH_LONG).show();
                }
                else if("".equals(phone)){
                    Toast.makeText(getApplicationContext(),"전화번호를 입력하세요.",Toast.LENGTH_LONG).show();
                }
                else if("".equals(email1) || "".equals(email2)){
                    Toast.makeText(getApplicationContext(),"이메일을 입력하세요",Toast.LENGTH_LONG).show();
                }
                else if("".equals(adr1_tf.getText().toString())){
                    Toast.makeText(getApplicationContext(),"도로명주소를 입력하세요.",Toast.LENGTH_LONG).show();
                }
                else if("".equals(adr2_tf.getText().toString())){
                    Toast.makeText(getApplicationContext(), "나머지주소를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(),Join3Activity.class);
                    intent.putExtra("memID", id);
                    intent.putExtra("memPW",pw);
                    intent.putExtra("memName",name);
                    intent.putExtra("memGender",gender);
                    intent.putExtra("memBirth", birth);
                    intent.putExtra("memPhone", phone);
                    intent.putExtra("memEmail",email);
                    intent.putExtra("memAdr1",address1);
                    intent.putExtra("memAdr2",address2);
                    startActivity(intent);
                }
            }


        });
    }
    public void init_webView() {
        daum_webView = (WebView) findViewById(R.id.daum_webview);
        daum_webView.getSettings().setJavaScriptEnabled(true);
        daum_webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        daum_webView.addJavascriptInterface(new AndroidBridge(), "TestApp");
        daum_webView.setWebChromeClient(new WebChromeClient());
        daum_webView.loadUrl("http://192.168.0.9/daum_address.php");
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setAddress(final String arg1, final String arg2, final String arg3) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    daum_webView = findViewById(R.id.daum_webview);
                    daum_webView.setVisibility(View.INVISIBLE);
                    adr1_tf.setText(String.format("(%s) %s %s", arg1, arg2, arg3));

                    init_webView();
                }
            });
        }
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
