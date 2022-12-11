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
import com.example.novamarket.Activity.Home_Chat;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.util.ArrayList;

public class Chat_join_adapter extends RecyclerView.Adapter<Chat_join_adapter.ViewHolder> {
    Context context;
    ArrayList<DTO_Chat> items = new ArrayList<>();

    public Chat_join_adapter(Context context) {
        this.context = context;
    }

    public void addItem(DTO_Chat item) {
        this.items.add(item);
        notifyItemInserted(items.size());
    }

    public void clearItems() {
        int size = items.size();
        this.items.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public Chat_join_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_join, parent, false);
        return new Chat_join_adapter.ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DTO_Chat item = items.get(position);
        Glide.with(context).load(item.getMsg_profile()).circleCrop().into(holder.item_image);
        Glide.with(context).load(item.getMsg_image()).centerCrop().into(holder.item_home_image);
        holder.item_title.setText(item.getMsg_name());
        holder.item_content.setText(item.getMsg());
        try {
            holder.item_date.setText(DateConverter.resultDateToString(item.getMsg_date(), "M월 dd일"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(item.getRoom_count() > 0){
            holder.item_count.setVisibility(View.VISIBLE);
            holder.item_count.setText(item.getRoom_count()+"");
        }else{
            holder.item_count.setVisibility(View.INVISIBLE);
            holder.item_count.setText(item.getRoom_count()+"");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_image, item_home_image;
        TextView item_title, item_content, item_date, item_count;

        public ViewHolder(@NonNull View itemView, Chat_join_adapter adapter) {
            super(itemView);
            item_image = itemView.findViewById(R.id.item_join_image);
            item_title = itemView.findViewById(R.id.item_join_title);
            item_content = itemView.findViewById(R.id.item_join_msg);
            item_date = itemView.findViewById(R.id.item_join_date);
            item_count = itemView.findViewById(R.id.item_join_count);
            item_home_image = itemView.findViewById(R.id.item_join_home_image);

            // chat_room
            itemView.setOnClickListener(v -> {
                DTO_Chat item = adapter.items.get(getBindingAdapterPosition());
                String user_id = PreferenceManager.getString(adapter.context, "user_id");
                Intent intent = new Intent(itemView.getContext(), Home_Chat.class);
                intent.putExtra("home_id", item.getHome_id());
                intent.putExtra("read_id", item.getUser_id());
                intent.putExtra("room_id", item.getRoom_id());
                Logger.d(item.getUser_id());
                adapter.context.startActivity(intent);
            });
        }
    }
}
