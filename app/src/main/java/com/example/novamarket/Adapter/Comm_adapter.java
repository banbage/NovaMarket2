package com.example.novamarket.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.novamarket.Activity.Comm_Read;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Comm;
import com.example.novamarket.Retrofit.DTO_Image;
import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.util.ArrayList;

import pereira.agnaldo.previewimgcol.ImageCollectionView;

public class Comm_adapter extends RecyclerView.Adapter<Comm_adapter.ViewHolder> {
    ArrayList<DTO_Comm> items = new ArrayList<>();
    Context context;

    public Comm_adapter(Context context) {
        this.context = context;
    }

    public void clearAll() {
        int pos = getItemCount();
        items.clear();
        notifyItemRangeRemoved(0, pos);
    }

    public void addItems(DTO_Comm item) {
        items.add(item);
        notifyItemInserted(getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comm, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DTO_Comm item = items.get(position);

        Glide.with(context).load(item.getUser_profile()).circleCrop().into(holder.item_profile);
        holder.item_state.setText(item.getCate());
        holder.item_content.setText(item.getContent());
        holder.item_name.setText(item.getUser_name());
        try {
            holder.item_date.setText(DateConverter.getUploadMinuteTime(item.getDate()));
//            holder.item_date.setText(DateConverter.resultDateToString(item.getDate(), "M월 d일 a h:mm"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(item.getLike() == 0) {
            holder.item_like.setText("좋아요");
        }else{
            holder.item_like.setText("좋아요 " + item.getLike());
        }
        if(item.getChat() == 0){
            holder.item_chat.setText("답글 ");
        }else {
            holder.item_chat.setText("답글 " + item.getChat());
        }
        if (item.getImages().size() > 0) {
            holder.item_images.setVisibility(View.VISIBLE);
            Glide.with(context).load(item.getImages().get(0).getImage()).into(holder.item_images);
        } else {
            holder.item_images.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_profile,  item_images;;
        TextView item_state, item_content, item_date, item_like, item_chat, item_name;
        ConstraintLayout item_like_btn, item_chat_btn;

        public ViewHolder(@NonNull View itemView, Comm_adapter adapter) {
            super(itemView);

            item_profile = itemView.findViewById(R.id.item_comm_profile);
            item_state = itemView.findViewById(R.id.item_comm_cate);
            item_content = itemView.findViewById(R.id.item_comm_content);
            item_date = itemView.findViewById(R.id.item_comm_date);
            item_like = itemView.findViewById(R.id.item_comm_like_txt);
            item_chat = itemView.findViewById(R.id.item_comm_chat_txt);
            item_name = itemView.findViewById(R.id.item_comm_name);
            item_images = itemView.findViewById(R.id.item_comm_images);

            itemView.setOnClickListener(v -> {
                String Comm_id = adapter.items.get(getBindingAdapterPosition()).getComm_id();
                Intent intent = new Intent(itemView.getContext(), Comm_Read.class);
                intent.putExtra("comm_id",Comm_id);
                itemView.getContext().startActivity(intent);
            });

        }
    }
}
