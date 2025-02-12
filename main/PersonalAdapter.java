package com.example.main;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class PersonalAdapter extends ArrayAdapter {

    private Context context;
    private List list;


    class fViewHolder{
        public TextView seq_view;
        public TextView date_view;
        public TextView symptom_view;
        public TextView doc_view;
    }

    public PersonalAdapter(Context context, ArrayList list){
        super(context,0,list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final fViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.custom_flist, parent, false);
        }

        viewHolder = new fViewHolder();
        viewHolder.seq_view = (TextView) convertView.findViewById(R.id.f_seq);
        viewHolder.date_view = (TextView) convertView.findViewById(R.id.f_date);
        viewHolder.symptom_view = (TextView) convertView.findViewById(R.id.f_symp);
        viewHolder.doc_view = (TextView) convertView.findViewById(R.id.f_doc);

        final Feedback feedback = (Feedback) list.get(position);
        viewHolder.seq_view.setText(feedback.getSeq());
        viewHolder.date_view.setText(feedback.getDate());
        viewHolder.symptom_view.setText(feedback.getSymptom());
        viewHolder.doc_view.setText(feedback.getDoc());

        return convertView;
    }
}
