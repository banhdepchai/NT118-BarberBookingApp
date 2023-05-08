package com.example.androidbarberapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.androidbarberapp.Model.Banner;
import com.example.androidbarberapp.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.SlideViewHolder> {

    private List<Banner> bannerList;
    private ViewPager2 viewPager2;

    public BannerAdapter(List<Banner> bannerList, ViewPager2 viewPager2){
        this.bannerList = bannerList;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         return new SlideViewHolder(
                 LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_item_container, parent, false)
         );
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.setImage(bannerList.get(position));
//        if(position == bannerList.size() - 2){
//            viewPager2.post(runnable);
//        }
    }

    @Override
    public int getItemCount() {
        return bannerList.size();
    }

    class SlideViewHolder extends RecyclerView.ViewHolder{
        private RoundedImageView imageView;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (RoundedImageView) itemView.findViewById(R.id.image_slide);

        }

        void setImage(Banner banner){
            imageView.setImageResource(banner.getImage());
        }
    }

}
