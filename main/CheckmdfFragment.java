package com.example.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.main.utils.Encryption;

public class CheckmdfFragment extends Fragment {
    private View rootView;
    private Button ok_btn;
    private EditText pw_tf;
    private String pw, age;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_checkmdf, container, false);

        ok_btn = rootView.findViewById(R.id.pwcheck_btn);
        pw_tf = rootView.findViewById(R.id.checkpw_tf);

        pw = getArguments().getString("PW");


        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editpw = Encryption.SHA256(pw_tf.getText().toString());
                if(pw.equals(editpw)){
                    ((MainActivity)getActivity()).deliverBundle();
                }
                else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setMessage("비밀번호를 입력하세요.")
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = dialog.create();
                    alert.show();
                }
            }
        });

        return rootView;
    }
}