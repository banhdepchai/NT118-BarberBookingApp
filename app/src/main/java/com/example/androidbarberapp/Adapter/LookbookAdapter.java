package com.example.androidbarberapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbarberapp.R;

import java.util.ArrayList;

public class LookbookAdapter extends RecyclerView.Adapter<LookbookAdapter.MyViewHolder>{

    Context context;
    ArrayList<Integer> lookbookList;

    public LookbookAdapter(Context context, ArrayList<Integer> lookbookList) {
        this.context = context;
        this.lookbookList = lookbookList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        public MyViewHolder(@NonNull ViewGroup parent) {
            super(parent);

            imageView = (ImageView) itemView.findViewById(R.id.image_look_book);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View lookbookView = inflater.inflate(R.layout.layout_look_book,parent,false);
        MyViewHolder viewHolder = new MyViewHolder((ViewGroup) lookbookView);
        return viewHolder;

//        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_look_book,parent,false);
//        return new MyViewHolder((ViewGroup) itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LookbookAdapter.MyViewHolder holder, int position) {
        holder.imageView.setImageResource(lookbookList.get(position));

    }

    @Override
    public int getItemCount() {
        return lookbookList.size();
    }
}
