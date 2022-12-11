package com.example.novamarket.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.novamarket.R;

import java.util.ArrayList;

public class Pager_Adapter extends RecyclerView.Adapter<Pager_Adapter.ViewPager> {
    ArrayList<String> items = new ArrayList<>();
    Context context;

    public Pager_Adapter(Context context) {
        this.context = context;
    }


    public void addItem(String item){
        this.items.add(item);
        notifyItemInserted(items.size());
    }

    public void clearItem(){
        int pos = items.size();
        this.items.clear();
        notifyItemRangeRemoved(0,pos);
    }

    @NonNull
    @Override
    public ViewPager onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image,parent,false);
        return new ViewPager(view,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewPager holder, int position) {
        String url = items.get(position);
        Glide.with(context).load(url).into(holder.item_image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class ViewPager extends RecyclerView.ViewHolder {
        private ImageView item_image;

        public ViewPager(@NonNull View itemView, Pager_Adapter adapter) {
            super(itemView);
            item_image = itemView.findViewById(R.id.item_pager_image);
        }
    }
}
