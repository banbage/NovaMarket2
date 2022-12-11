package com.example.novamarket.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Home;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class Image_adapter extends RecyclerView.Adapter<Image_adapter.ViewHolder> implements ItemTouchHelperListener{
    ArrayList<Bitmap> items = new ArrayList<>();
    Context context;

    public Image_adapter(Context context) {
        this.context = context;
    }

    public void addItem(Bitmap item) {
        this.items.add(item);
        notifyItemInserted(items.size());
        EventBus.getDefault().post(new SendEvent(getItemCount()));
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_write_img, parent, false);
        return new ViewHolder(view,this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap bitmap = items.get(position);
        if(position == 0){
            holder.item_txt.setVisibility(View.VISIBLE);
        }else{
            holder.item_txt.setVisibility(View.GONE);
        }
        Glide.with(context).load(bitmap).into(holder.item_image);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public boolean onItemMove(int from_position, int to_position) {
        // 이동 객체 저장
        Bitmap item = items.get(from_position);
        // 이동시킬 객체 삭제
        items.remove(from_position);
        // 이동 시킬 객체 이동
        items.add(to_position, item);
        // 데이터 이동 알림
        notifyItemMoved(from_position, to_position);
        notifyItemChanged(from_position);
        notifyItemChanged(to_position);
        return true;
    }

    @Override
    public void onItemSwipe(int position) {

    }

    public ArrayList<Bitmap> getItems() {
        return items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_image;
        TextView item_txt;
        ImageView item_delete;

        public ViewHolder(@NonNull View itemView, Image_adapter adpater) {
            super(itemView);
            item_image = itemView.findViewById(R.id.item_homeWrite_image);
            item_txt = itemView.findViewById(R.id.item_homeWrite_txt);
            item_delete = itemView.findViewById(R.id.item_homeWrite_delete);

            item_delete.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION){
                    adpater.items.remove(pos);
                    adpater.notifyItemRemoved(pos);
                    adpater.notifyItemChanged(0);
                    Logger.d("이벤트 버스 실행");
                    EventBus.getDefault().post(new SendEvent(adpater.items.size()));
                }
            });
        }
    }
}
