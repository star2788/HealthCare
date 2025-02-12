package com.example.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter {
    private Context context;
    private List list;

    class ViewHolder{
        public TextView num_view;
        public TextView hospital_view;
        public TextView date_view;
        public TextView time_view;
    }

    public CustomAdapter(Context context, ArrayList list){
        super(context,0, list);
        this.context = context;
        this.list = list;
        System.out.println("어댑터 : "+this.list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final ViewHolder viewHolder;

        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.custom_rlist, parent, false);
        }

        viewHolder = new ViewHolder();
        viewHolder.num_view = (TextView) convertView.findViewById(R.id.r_num);
        viewHolder.hospital_view = (TextView) convertView.findViewById(R.id.r_hospital);
        viewHolder.date_view = (TextView) convertView.findViewById(R.id.r_date);
        viewHolder.time_view = (TextView) convertView.findViewById(R.id.r_time);

        final Reservation reservation = (Reservation) list.get(position);
        System.out.println("dd: "+reservation.getResnum());
        viewHolder.num_view.setText(reservation.getSeq());
        viewHolder.hospital_view.setText(reservation.getHospital());
        viewHolder.date_view.setText(reservation.getDate());
        viewHolder.time_view.setText(reservation.getTime());

        return convertView;
    }
}
