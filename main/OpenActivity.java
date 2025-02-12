package com.example.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OpenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        Button gologin_btn = (Button) findViewById(R.id.gotologin_btn);
        Button gojoin_btn = (Button) findViewById(R.id.gotojoin_btn);

        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        if(auto.getBoolean("autoLogin",false)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("userName", auto.getString("userName",""));
            intent.putExtra("userBlood", auto.getString("userBlood",""));
            intent.putExtra("userBirth", auto.getString("userBirth",""));
            intent.putExtra("userID", auto.getString("userID",""));
            intent.putExtra("userPW", auto.getString("userPW",""));
            intent.putExtra("userMBL", auto.getString("TEST",""));

            Toast.makeText(getApplicationContext(), "자동 로그인되었습니다.", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }

        gologin_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });
        gojoin_btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),JoinActivity.class);
                startActivity(intent);
            }
        });
    }
}