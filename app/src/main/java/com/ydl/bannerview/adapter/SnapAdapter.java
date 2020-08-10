package com.ydl.bannerview.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.ydl.bannerview.R;

import java.util.ArrayList;

public class SnapAdapter extends RecyclerView.Adapter<SnapAdapter.MyViewHolder> {


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_snap, null);
        return new MyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.imageView.setBackgroundResource(R.drawable.icon111);
        holder.textView.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    private ArrayList<String> list ;
    public void setData(ArrayList<String> list){
        this.list = list;
    }
     class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final TextView textView;

        MyViewHolder(View parent) {
            super(parent);
            imageView = parent.findViewById(R.id.iv_image);
            textView = parent.findViewById(R.id.tv_title);
        }

    }
}
