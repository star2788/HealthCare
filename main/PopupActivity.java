package com.example.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.ReservationRequest;

import org.json.JSONObject;

public class PopupActivity extends Activity {
    private TextView resnum_tf, resdoc_tf, resdate_tf, restime_tf, resroom_tf, reshpt_tf;
    private Button btn_cancel, btn_ok;
    private String res_number, doc, date, time, room, hospital;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_popup);

        Intent intent = getIntent();
        res_number = intent.getStringExtra("res_number");
        doc = intent.getStringExtra("doc");
        date = intent.getStringExtra("date");
        time = intent.getStringExtra("time");
        room = intent.getStringExtra("room");
        hospital = intent.getStringExtra("hospital");

        resnum_tf = findViewById(R.id.popup_resnum);
        resdoc_tf = findViewById(R.id.popup_resdoc);
        resroom_tf = findViewById(R.id.popup_resroom);
        resdate_tf = findViewById(R.id.popup_resdate);
        restime_tf = findViewById(R.id.popup_restime);
        reshpt_tf = findViewById(R.id.popup_reshpt);
        btn_cancel = findViewById(R.id.popup_btn_cancel);
        btn_ok = findViewById(R.id.popup_btn_ok);

        resnum_tf.setText(res_number);
        resdoc_tf.setText(doc);
        resdate_tf.setText(date);
        restime_tf.setText(time);
        resroom_tf.setText(room);
        reshpt_tf.setText(hospital);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ad = new AlertDialog.Builder(PopupActivity.this);
                ad.setMessage("정말 취소하시겠습니까?");
                ad.setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Response.Listener<String> responseListener_send = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    boolean success = jsonObject.getBoolean("success");

                                    System.out.println(success+"   ----------");
                                    if(success){
                                        Toast.makeText(getApplicationContext(),"예약이 취소되었습니다.",Toast.LENGTH_SHORT).show();
                                        ((MainActivity)MainActivity.mContext).replaceFragment(ConfirmFragment.getInstance());
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(),"예약 취소에 실패했습니다.",Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        ReservationRequest cancelRequest = new ReservationRequest(res_number, responseListener_send);
                        RequestQueue queue = Volley.newRequestQueue(PopupActivity.this);
                        queue.add(cancelRequest);

                    }
                });
                ad.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                ad.show();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
}