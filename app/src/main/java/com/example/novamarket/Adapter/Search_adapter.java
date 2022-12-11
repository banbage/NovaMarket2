package com.example.novamarket.Adapter;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
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
import com.example.novamarket.Retrofit.DTO_Search;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class Search_adapter extends RecyclerView.Adapter<Search_adapter.viewHolder> {
    Context context;
    ArrayList<DTO_Search> items = new ArrayList<>();

    public Search_adapter(Context context) {
        this.context = context;
    }

    public void addItem(DTO_Search item){
        items.add(item);
        notifyItemInserted(items.size());
    }

    public void clearItems(){
        int size = items.size();
       items.clear();
        notifyItemRangeRemoved(0, size);
    }

    public ArrayList<DTO_Search> getItems(){
        return items;
    }

    public ArrayList<String> getContents(){
        ArrayList<String> contents = new ArrayList<>();
        // 현재 검색어 리스트 데이터 싹 정리해서 보내줌
        for(int i = 0; i < items.size(); i++) {
            contents.add(items.get(i).getSearch());
        }
        return contents;
    }

    @NonNull
    @Override
    public Search_adapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search,parent,false);
        return new viewHolder(item, this);
    }

    @Override
    public void onBindViewHolder(@NonNull Search_adapter.viewHolder holder, int position) {
        DTO_Search item = items.get(position);
        holder.item_tv.setText(item.getSearch());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        private TextView item_tv;
        private ImageView item_x;

        public viewHolder(@NonNull View itemView, Search_adapter adapter) {
            super(itemView);

            item_tv = itemView.findViewById(R.id.item_search_tv);
            item_x = itemView.findViewById(R.id.item_search_delete);

            // 제거
            item_x.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION){
                    adapter.items.remove(pos);
                    adapter.notifyItemRemoved(pos);
                }
            });

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if(pos != RecyclerView.NO_POSITION){
                    String search = adapter.items.get(pos).getSearch();
                    EventBus.getDefault().post(new SearchEvent(search,"search")); // 검색어 입력
                    EventBus.getDefault().post(new CheckEvent(true)); // 엑티비티 종료
                }
            });
        }
    }
}

