package com.example.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.main.utils.Encryption;

public class checkpw_cdFragment extends Fragment {

    private Button checkbtn;
    private TextView checktf;
    private String userID, userPW, userDoc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkpw_cd, container, false);

        checkbtn = view.findViewById(R.id.pwcheck_btn);
        checktf = view.findViewById(R.id.checkpw_tf);

        userID = getArguments().getString("userID");
        userPW = getArguments().getString("userPW");
        userDoc = getArguments().getString("userDoc");
        System.out.println("아아 : "+userDoc);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        checkbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pw = Encryption.SHA256(checktf.getText().toString());
                if(userPW.equals(pw)) {

                    Bundle bundle = new Bundle();
                    bundle.putString("userID",userID);
                    bundle.putString("userPW",userPW);
                    bundle.putString("userDoc",userDoc);
                    conDocFragment condocFragment = new conDocFragment();
                    condocFragment.setArguments(bundle);
                    transaction.replace(R.id.main_layout,condocFragment);
                    transaction.commit();
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


        return view;
    }
}