package com.example.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class conDocFragment extends Fragment {

    private Button cc_btn;
    private TextView cc_tf;

    private String userID, userDoc;
    private AES256Chiper aes256Chiper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_con_doc, container, false);

        aes256Chiper = new AES256Chiper();
        cc_btn = view.findViewById(R.id.ok_btn);
        cc_tf = view.findViewById(R.id.concode_tf);

        userID = getArguments().getString("userID");
        userDoc = getArguments().getString("userDoc");

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        SharedPreferences auto = requireActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = auto.edit();

        cc_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                String code = cc_tf.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) {
                                String docID = jsonObject.getString("docID");
                                String docName = jsonObject.getString("docName");
                                cc_tf.setText("");
                                editor.putString("conDoc",docID);
                                editor.putString("docName",docName);
                                editor.commit();

                                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                                dialog.setMessage("담당의사와 연결되었습니다.");
                                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                                dialog.show();
                            } else {
                                Toast.makeText(getContext(),"연결코드를 다시 확인해주세요.",Toast.LENGTH_SHORT).show();
                                cc_tf.setText("");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                try {
                    String e_id = aes256Chiper.encoding(userID,MainActivity.key);
                    String e_code = aes256Chiper.encoding(code, MainActivity.key);
                    System.out.println("의사연결 : "+e_id+" // "+e_code);
                    MemberRequest condocRequest = new MemberRequest(3, e_id,e_code, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(getContext());
                    queue.add(condocRequest);
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


        return view;
    }
}