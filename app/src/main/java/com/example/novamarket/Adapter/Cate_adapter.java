package com.example.novamarket.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.novamarket.EventBus.CheckEvent;
import com.example.novamarket.EventBus.SearchEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Cate;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class Cate_adapter extends RecyclerView.Adapter<Cate_adapter.ViewHolder> {
    ArrayList<DTO_Cate> items = new ArrayList<>();
    Context context;

    public Cate_adapter(Context context) {
        this.context = context;
    }

    public void addItem(DTO_Cate item){
        items.add(item);
        notifyItemInserted(getItemCount());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cate,parent,false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DTO_Cate item = items.get(position);
        holder.item_image.setImageResource(item.getImage());
        holder.item_content.setText(item.getContent());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView item_image;
        TextView item_content;

        public ViewHolder(@NonNull View itemView, Cate_adapter adapter) {
            super(itemView);
            item_image = itemView.findViewById(R.id.item_cate_image);
            item_content = itemView.findViewById(R.id.item_cate_tv);

            itemView.setOnClickListener(v -> {
                String str = items.get(getBindingAdapterPosition()).getContent();
                EventBus.getDefault().post(new CheckEvent(true));
                EventBus.getDefault().post(new SearchEvent(str,"cate"));
            });
        }
    }
}
