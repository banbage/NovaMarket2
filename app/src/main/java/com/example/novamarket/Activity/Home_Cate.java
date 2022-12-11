package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;

import com.example.novamarket.Adapter.Cate_adapter;
import com.example.novamarket.EventBus.CheckEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Cate;
import com.example.novamarket.databinding.ActivityHomeCateBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Home_Cate extends AppCompatActivity {
    private ActivityHomeCateBinding bind;
    private Cate_adapter adapter;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void finishEvent(CheckEvent event){
        if(event.check){
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeCateBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(),3,GridLayoutManager.VERTICAL,false);
        bind.cateRv.setLayoutManager(gridLayoutManager);
        adapter = new Cate_adapter(getBaseContext());
        bind.cateRv.setAdapter(adapter);

        adapter.addItem(new DTO_Cate(R.drawable.all, "전체"));
        adapter.addItem(new DTO_Cate(R.drawable.laptop, "디지털기기"));
        adapter.addItem(new DTO_Cate(R.drawable.furniture, "가구/인테리어"));
        adapter.addItem(new DTO_Cate(R.drawable.kitchen, "생활/주방"));
        adapter.addItem(new DTO_Cate(R.drawable.fasion, "패션/잡화"));
        adapter.addItem(new DTO_Cate(R.drawable.book, "도서"));
        adapter.addItem(new DTO_Cate(R.drawable.plant, "식물"));
        adapter.addItem(new DTO_Cate(R.drawable.game, "취미/게임/음반"));
    }
}