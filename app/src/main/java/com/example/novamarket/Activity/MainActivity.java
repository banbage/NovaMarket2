package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.novamarket.R;
import com.example.novamarket.databinding.ActivityMainBinding;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding bind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());


        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)// (Optional) Whether to show thread info or not. Default true
                .methodCount(2)// (Optional) How many method line to show. Default 2
                .methodOffset(0)// (Optional) Hides internal method calls up to offset. Default 5
                .tag("PRETTY_LOGGER")// (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        // 일반적인 로그 찍기
        Logger.e("message");

    }
}