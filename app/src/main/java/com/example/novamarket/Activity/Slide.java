package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.novamarket.EventBus.StateEvent;
import com.example.novamarket.R;
import com.example.novamarket.databinding.ActivitySlideBinding;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

public class Slide extends Activity {
    private ActivitySlideBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivitySlideBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        // 판매중
        bind.slideSell.setOnClickListener(v -> {
            EventBus.getDefault().post(new StateEvent("판매중"));
            finish();
        });

        //예약중
        bind.slideReserve.setOnClickListener(v -> {
            EventBus.getDefault().post(new StateEvent("예약중"));
            finish();

        });

        //판매완료
        bind.slideSellEnd.setOnClickListener(v -> {
            EventBus.getDefault().post(new StateEvent("판매완료"));
            finish();
        });

        bind.slideFinish.setOnClickListener(v -> {
            finish();
        });
    }
}