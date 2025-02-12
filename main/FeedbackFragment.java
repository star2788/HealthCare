package com.example.main;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.FeedbackRequest;
import com.example.main.Request.FeedbackRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class FeedbackFragment extends Fragment {

    private ArrayList<Feedback> feedbacks;
    private ListView personalListView;
    private static PersonalAdapter perAdapter;
    private String id;
    private Context ct;
    private Bundle mBundle;
    private AES256Chiper aes256Chiper;

    public static FeedbackFragment newInstance() {
        return new FeedbackFragment();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fback, container, false);

        ct = container.getContext();
        personalListView = (ListView)rootView.findViewById(R.id.custom_flist);
        aes256Chiper = new AES256Chiper();

        id = getArguments().getString("userID");
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    feedbacks = new ArrayList<>();
                    perAdapter = new PersonalAdapter(getContext(), feedbacks);
                    personalListView.setAdapter(perAdapter);

                    JSONArray jsonArray = new JSONArray(response);
                    int i = 0;
                    while(i < jsonArray.length()){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        boolean success = jsonObject.getBoolean("success");
                        if(success){
                            String feedback = aes256Chiper.decoding(jsonObject.getString("fContent"), MainActivity.key);
                            String date = jsonObject.getString("fDate");
                            String nndate = jsonObject.getString("fnnDate");
                            String symptom = aes256Chiper.decoding(jsonObject.getString("fSymptom"),MainActivity.key);
                            String doctor = aes256Chiper.decoding(jsonObject.getString("docName"),MainActivity.key);
                            System.out.println("d왜 의사가 아이디가 아니지? : "+doctor);
                            feedbacks.add(new Feedback(i+1, date, nndate, symptom,doctor,feedback ));
                        }
                        else{

                        }
                        i++;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        try {
            String e_id = aes256Chiper.encoding(id,MainActivity.key);
            FeedbackRequest feedbackRequest = new FeedbackRequest(e_id, responseListener);
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            queue.add(feedbackRequest);
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



        personalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                mBundle = new Bundle();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

                mBundle.putString("userID",id);
                mBundle.putString("docName",feedbacks.get(position).getDoc());
                mBundle.putString("fContent",feedbacks.get(position).getContent());
                mBundle.putString("fDate",feedbacks.get(position).getDate());
                mBundle.putString("fnnDate",feedbacks.get(position).getNndate());
                mBundle.putString("fSymptom",feedbacks.get(position).getSymptom());
                FbconFragment fbconFragment = new FbconFragment();
                fbconFragment.setArguments(mBundle);
                transaction.replace(R.id.main_layout,fbconFragment);
                transaction.commit();
            }
        });


        return rootView;
    }
}
class Feedback {
    private String seq;
    private String date;
    private String symptom;
    private String doc;
    private String content;
    private String nndate;


    public Feedback(int seq, String date,String nndate, String symptom, String doc, String content) {
        this.seq = Integer.toString(seq);
        this.doc = doc;
        this.date = date;
        this.nndate = nndate;
        this.symptom = symptom;
        this.content = content;

    }

    public String getSeq() {
        return seq;
    }

    public String getDoc() {
        return doc;
    }

    public String getDate() {
        return date;
    }

    public String getSymptom(){
        return symptom;
    }

    public String getContent(){ return content; }

    public String getNndate(){ return nndate; }


}
