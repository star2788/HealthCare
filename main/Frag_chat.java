package com.example.main;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.main.Request.ChatRequest;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.example.main.MainActivity.chatDataArrayList;

public class Frag_chat extends Fragment{

    private static final int MSG_CHAT_RECEIVE_READY = 0;
    private static final int MSG_CHAT_RECEIVE_END = 1;
    private static final int MSG_CHAT_RECEIVE_RESTART = 2;
    private View view;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<ChatData> chatList;
    private String sender=""; // 내 닉네임
    private String receiver=""; //의사 이름 나중에 받아와야함
    private String conDoc = "";
    private String msg;
    private EditText editText_chat;
    private Button button_send;
    private Date date;
    private ChatData chatData,chatData2;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private AES256Chiper aes256Chiper;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_chat , container , false);

        button_send = view.findViewById(R.id.Button_send);
        editText_chat = view.findViewById(R.id.EditText_chat);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        sender = getArguments().getString("userID");
        conDoc = getArguments().getString("conDoc");

        System.out.println("연결된 의사 : "+conDoc);
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList,getContext(), sender);
        recyclerView.setAdapter(adapter);

        aes256Chiper = new AES256Chiper();

        int i =0;

        while (chatDataArrayList.size()>i){
            ChatData mCD = chatDataArrayList.get(i);
            ((ChatAdapter)adapter).addChat(mCD);
            i++;
        }
        startListening();
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);


        button_send.setOnClickListener(new View.OnClickListener() { //채팅 보내기
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                msg = editText_chat.getText().toString();
                System.out.println(msg);
                editText_chat.setText("");
                date = new Date();
                chatData = new ChatData(msg,format.format(date),sender,receiver);

                chatDataArrayList.add(chatData);

                ((ChatAdapter)adapter).addChat(chatData);

                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);


                Response.Listener<String> responseListener_send = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                    }
                };

                try {
                    String e_msg = aes256Chiper.encoding(msg, MainActivity.key);
                    String e_format = aes256Chiper.encoding(format.format(date), MainActivity.key);
                    String e_sender = aes256Chiper.encoding(sender, MainActivity.key);
                    String e_conDoc = aes256Chiper.encoding(conDoc, MainActivity.key);
                    String e_send = aes256Chiper.encoding("1",MainActivity.key);
                    String e_chk = aes256Chiper.encoding("1",MainActivity.key);

                    ChatRequest chatRequest = new ChatRequest(e_msg,e_format,e_sender,e_conDoc,e_send,e_chk, responseListener_send);
                    RequestQueue queue = Volley.newRequestQueue(getContext());
                    queue.add(chatRequest);

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

    @Override
    public void onDestroy() {
        handler.sendEmptyMessage(MSG_CHAT_RECEIVE_READY);
        handler.removeCallbacksAndMessages(null);
        handler.removeMessages(0);
        super.onDestroy();
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case MSG_CHAT_RECEIVE_READY	: break;
                case MSG_CHAT_RECEIVE_END		:
                {
                    stopListening();
                    sendEmptyMessageDelayed(MSG_CHAT_RECEIVE_RESTART, 1000);
                    break;
                }
                case MSG_CHAT_RECEIVE_RESTART	: startListening();	break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    public void startListening(){
        handler.sendEmptyMessage(MSG_CHAT_RECEIVE_END);

        if(true == getArguments().getBoolean("Check")){

            msg = getArguments().getString("Msg");
            String date = getArguments().getString("Date");
            String sender = getArguments().getString("Sender");
            receiver = getArguments().getString("Receiver");

            chatData = new ChatData(msg,date,sender,receiver);
            ((ChatAdapter)adapter).addChat(chatData,ChatService.last_number);


            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount() - 1);

        }
    }

    public void stopListening(){

    }

}

