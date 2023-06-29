package com.example.androidbarberapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidbarberapp.Common.Common;
import com.example.androidbarberapp.Interface.IRecyclerItemSelectedListener;
import com.example.androidbarberapp.Model.Barber;
import com.example.androidbarberapp.Model.EventBus.EnableNextButton;
import com.example.androidbarberapp.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class MyBarberAdapter extends RecyclerView.Adapter<MyBarberAdapter.MyViewHolder> {

    Context context;
    List<Barber> barberList;
    List<CardView> cardViewList;


    public MyBarberAdapter(Context context, List<Barber> barberList) {
        this.context = context;
        this.barberList = barberList;
        cardViewList = new ArrayList<>();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txt_barber_name;
        RatingBar ratingBar;
        CardView card_barber;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_barber_name = (TextView)itemView.findViewById(R.id.txt_barber_name);
            ratingBar = (RatingBar)itemView.findViewById(R.id.rtb_barber);
            card_barber = (CardView)itemView.findViewById(R.id.card_barber);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_barber, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBarberAdapter.MyViewHolder holder, int position) {
        holder.txt_barber_name.setText(barberList.get(position).getName());
        if(barberList.get(position).getRatingTimes() != 0)
        {
            holder.ratingBar.setRating(barberList.get(position).getRating().floatValue() / barberList.get(position).getRatingTimes());
        }
        else
            holder.ratingBar.setRating(0);
        if(!cardViewList.contains(holder.card_barber))
            cardViewList.add(holder.card_barber);

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int pos) {
                // Set white background for all card not be selected
                for(CardView cardView:cardViewList)
                    cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));

                // Set selected BG for only selected item
                holder.card_barber.setCardBackgroundColor(context.getResources()
                .getColor(android.R.color.holo_orange_dark));


                //Event Bus
                EventBus.getDefault().postSticky(new EnableNextButton(2, barberList.get(pos)));
            }
        });
    }

    @Override
    public int getItemCount() {
        return barberList.size();
    }


}
