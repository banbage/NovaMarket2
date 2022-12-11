package com.example.novamarket.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.novamarket.EventBus.ImageEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class Add_adpater extends RecyclerView.Adapter<Add_adpater.ViewHolder> {
    Context context;
    ArrayList<DTO_Chat> items = new ArrayList<>();

    public Add_adpater(Context context) {
        this.context = context;
    }

    public void addItem(DTO_Chat item){
        this.items.add(item);
        notifyItemInserted(items.size());
    }

    @NonNull
    @Override
    public Add_adpater.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add,parent,false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull Add_adpater.ViewHolder holder, int position) {
        DTO_Chat item = items.get(position);
        Glide.with(context).load(item.getDrawable()).into(holder.item_image);
        holder.item_name.setText(item.getMsg_name());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_image;
        TextView item_name;

        public ViewHolder(@NonNull View itemView, Add_adpater adapter) {
            super(itemView);
            item_image = itemView.findViewById(R.id.item_add_image);
            item_name = itemView.findViewById(R.id.item_add_name);

            itemView.setOnClickListener(v -> {
                String txt = item_name.getText().toString();
                switch (txt){
                    case "이미지" :
                        sendImages(adapter);
                        break;
                    case "장소" :
                        sendLocation(adapter);
                        break;
                    default:
                }
            });

        }

        private void sendLocation(Add_adpater adapter) {
            EventBus.getDefault().post(new ImageEvent("장소"));
        }

        private void sendImages(Add_adpater adapter) {
            EventBus.getDefault().post(new ImageEvent("이미지"));
        }
    }
}
