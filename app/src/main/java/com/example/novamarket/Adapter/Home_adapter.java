package com.example.novamarket.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.novamarket.Activity.Home_Read;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Home;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class Home_adapter extends RecyclerView.Adapter<Home_adapter.ViewHolder> {
    ArrayList<DTO_Home> items = new ArrayList<>();
    Context context;

    public Home_adapter(Context context) {
        this.context = context;
    }

    public void addItem(DTO_Home item){
        this.items.add(item);
        notifyItemInserted(items.size());
    }

    public void clearAll(){
        int size = items.size();
        this.items.clear();
        notifyItemRangeRemoved(0,size);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home,parent,false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DTO_Home item = items.get(position);
        Glide.with(context).load(item.getImage()).centerCrop().into(holder.item_image);
        holder.item_title.setText(item.getTitle());
        DecimalFormat decimalFormat = new DecimalFormat("###,###");
        String price = decimalFormat.format(item.getPrice());
        holder.item_price.setText(price + "원");
        try {
            holder.item_date.setText(DateConverter.resultDateToString(item.getDate(), "M월 d일 a h:mm"));
//            holder.item_date.setText(DateConverter.getUploadMinuteTime(item.getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(item.getState().equals("판매중")){
            holder.item_state.setVisibility(View.GONE);
        }else{
            holder.item_state.setVisibility(View.VISIBLE);
            holder.item_state.setText(item.getState());
        }

        if(item.getLike() > 0) {
            holder.item_like.setVisibility(View.VISIBLE);
            holder.item_like_txt.setVisibility(View.VISIBLE);
            holder.item_like_txt.setText(""+item.getLike());
        }else{
            holder.item_like.setVisibility(View.GONE);
            holder.item_like_txt.setVisibility(View.GONE);
            holder.item_like_txt.setText(""+item.getLike());
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView item_image, item_like, item_chat;
        TextView item_title, item_date, item_price, item_like_txt, item_chat_txt, item_state;

        public ViewHolder(@NonNull View itemView, Home_adapter adapter) {
            super(itemView);

            item_image = itemView.findViewById(R.id.item_home_image);
            item_title = itemView.findViewById(R.id.item_home_title);
            item_date = itemView.findViewById(R.id.item_home_date);
            item_price = itemView.findViewById(R.id.item_home_price);
            item_like = itemView.findViewById(R.id.item_home_like);
            item_chat = itemView.findViewById(R.id.item_home_chat);
            item_like_txt = itemView.findViewById(R.id.item_home_like_txt);
            item_chat_txt = itemView.findViewById(R.id.item_home_chat_txt);
            item_state = itemView.findViewById(R.id.item_home_state);

            // 게시글 읽기
            itemView.setOnClickListener(v -> {
                String home_id = items.get(getBindingAdapterPosition()).getHome_id();
                Intent intent = new Intent(itemView.getContext(), Home_Read.class);
                intent.putExtra("home_id", home_id);
                context.startActivity(intent);
            });
        }
    }
}
