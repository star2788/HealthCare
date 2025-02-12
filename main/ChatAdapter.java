package com.example.main;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<ChatData> mDataset;
    private String myNickName;
    private String date;
    private int cmp=0;

    public static class MyViewHolder extends ViewHolder{
        public TextView Textview_nickname;
        public TextView Textview_msg;
        public TextView Textview_date;
        public View rootView;


        public MyViewHolder(View v){
            super(v);
            Textview_nickname = v.findViewById(R.id.row_chat_nickname);
            Textview_msg = v.findViewById(R.id.row_chat_message);
            Textview_date = v.findViewById(R.id.row_chat_date);
            rootView = v;
        }

    }

    public static class MyViewHolder2 extends ViewHolder{
        public TextView Textview2_msg;
        public TextView Textview2_date;
        public View rootView;


        public MyViewHolder2(View v){
            super(v);
            Textview2_msg = v.findViewById(R.id.row_chat2_message);
            Textview2_date = v.findViewById(R.id.row_chat2_date);
            rootView = v;
        }
    }


    public ChatAdapter(List<ChatData> myDataset, Context context, String myNickName){
        mDataset = myDataset;
        this.myNickName = myNickName;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat2,parent,false);
                return new MyViewHolder2(view);

            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat1,parent,false);
                return new MyViewHolder(view);
        }

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat1,parent,false);
        return new MyViewHolder(view);

       // LinearLayout v = (LinearLayout) LayoutInflater.
       //         from(parent.getContext()).inflate(R.layout.row_chat,parent,false);
       // MyViewHolder vh = new MyViewHolder(v);
        //return vh;


    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatData chat = mDataset.get(position);

        //holder.Textview_nickname.setText(chat.getSender());
        //holder.Textview_msg.setText(chat.getMsg()); //DTO


        System.out.println("메세지 정보들 : ");
        System.out.println(this.myNickName);
        System.out.println(chat.getReceiver());
        System.out.println(chat.getDate());
        String date = chat.getDate().substring(0,chat.getDate().length()-3);


        if(chat.getSender().equals(this.myNickName)){
            MyViewHolder2 holder2 = (MyViewHolder2) holder;
            holder2.Textview2_msg.setText(chat.getMsg());
            holder2.Textview2_date.setText(date);
        }
        else{
            MyViewHolder holder1 = (MyViewHolder) holder;
            holder1.Textview_nickname.setText(chat.getSender());
            holder1.Textview_msg.setText(chat.getMsg());
            holder1.Textview_date.setText(date);
        }
    }
    @Override
    public int getItemViewType(int position) {
        ChatData chatData = mDataset.get(position);
        if (chatData.getSender().equals(myNickName)) { // 내 아이디인 경우 오른쪽뷰로 분기 (0)
            return 0;
        } else { // 왼쪽뷰 (1)
            return 1;
        }
    }

    @Override
    public int getItemCount() {
        return mDataset == null ? 0 :  mDataset.size();
    }

    public ChatData getChat(int position){
        return mDataset != null ? mDataset.get(position) : null;
    }

    public void addChat (ChatData chat, int num){
        if(cmp != num){
            mDataset.add(chat);
            notifyItemInserted(mDataset.size()-1); //갱신용
        }
        else{
        }
        cmp = num;
    }


    public void addChat(ChatData chat){
        mDataset.add(chat);
        notifyItemInserted(mDataset.size()-1); //갱신용
        cmp = ChatService.last_number;

    }


}
