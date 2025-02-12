package com.example.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.ReservationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class ConfirmFragment extends Fragment {

    private ArrayList<Reservation> reservations;
    private ListView customListView;
    private static CustomAdapter customAdapter;
    private Context ct;
    private AES256Chiper aes256Chiper;

    public static Fragment getInstance() {
        return new ConfirmFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_confirm, container, false);

        ct = container.getContext();
        customListView = (ListView)rootView.findViewById(R.id.custom_rlist);
        aes256Chiper = new AES256Chiper();

        String id = getArguments().getString("userID");
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                try {
                    reservations = new ArrayList<>();
                    customAdapter = new CustomAdapter(getContext(),reservations);
                    customListView.setAdapter(customAdapter);

                    JSONArray jsonArray = new JSONArray(response);
                    int i = 0;
                    while(i < jsonArray.length()){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        boolean success = jsonObject.getBoolean("success");
                        if(success){
                            String num = jsonObject.getString("resSeq");
                            String time = aes256Chiper.decoding(jsonObject.getString("resTime"),MainActivity.key);
                            String date = aes256Chiper.decoding(jsonObject.getString("resDate"),MainActivity.key);
                            String doctor = aes256Chiper.decoding(jsonObject.getString("docName"),MainActivity.key);
                            String hospital = aes256Chiper.decoding(jsonObject.getString("docWork"),MainActivity.key);
                            String room = aes256Chiper.decoding(jsonObject.getString("docRoom"),MainActivity.key);

                            System.out.println("예약정보 : "+num+" : "+time+" : "+date+" : "+doctor+" : ");

                            String seq = i+1+"";

                            reservations.add(new Reservation(seq,num,hospital,doctor,date,time,room));
                        }
                        else{

                        }

                        i++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        };
        String idid = null;
        try {
            idid = aes256Chiper.encoding(id, MainActivity.key);
            ReservationRequest confirmRequest = new ReservationRequest("confirm",idid, responseListener);
            RequestQueue queue = Volley.newRequestQueue(ct);
            queue.add(confirmRequest);
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



        customListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), PopupActivity.class);
                intent.putExtra("res_number", reservations.get(position).getResnum());
                intent.putExtra("hospital", reservations.get(position).getHospital());
                intent.putExtra("doc", reservations.get(position).getDoc());
                intent.putExtra("date", reservations.get(position).getDate());
                intent.putExtra("time", reservations.get(position).getTime());
                intent.putExtra("room", reservations.get(position).getRoom());
                startActivityForResult(intent,1);
            }
        });

        return rootView;
    }
}


class Reservation {
    private String seq;
    private String res_number;
    private String hospital;
    private String doc;
    private String date;
    private String time;
    private String room;

    public Reservation(String seq, String num, String hospital, String doc, String date, String time, String room) {
        this.seq = seq;
        this.res_number = num;
        this.hospital = hospital;
        this.doc = doc;
        this.date = date;
        this.time = time;
        this.room = room;
        System.out.println(this.res_number);
    }

    public String getSeq() { return seq; }

    public String getResnum() {
        return res_number;
    }

    public String getHospital(){ return hospital; }

    public String getDoc() {
        return doc;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getRoom() {
        return room;
    }
}