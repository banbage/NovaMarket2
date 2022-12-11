package com.example.novamarket.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;

import com.example.novamarket.Adapter.ViewPager_adpater;
import com.example.novamarket.Fragment.Frag_Email;
import com.example.novamarket.Fragment.Frag_Phone;
import com.example.novamarket.R;
import com.example.novamarket.databinding.ActivityJoinBinding;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.orhanobut.logger.Logger;

public class Join extends AppCompatActivity {
    private ActivityJoinBinding bind;
    private Frag_Email frag_email;
    private Frag_Phone frag_phone;
    private ViewPager2 viewPager2;
    private TableLayout tableLayout;
    private ViewPager_adpater viewPager_adpater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityJoinBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());


        CreateFragment();
        CreateViewPager();
        settingTablayout();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String smsNum = intent.getStringExtra("sms_number");
        frag_phone.setAuthNum(smsNum);
        Logger.d("smsNum : " + smsNum);
    }

    private void CreateFragment() {
        frag_email = Frag_Email.newInstance();
        frag_phone = Frag_Phone.newInstance();
    }

    private void CreateViewPager() {
        viewPager_adpater = new ViewPager_adpater(this);
        viewPager_adpater.addItem(frag_email);
        viewPager_adpater.addItem(frag_phone);
        bind.joinViewpager.setAdapter(viewPager_adpater);
    }

    private void settingTablayout() {
        // Tab 레이아웃과 ViewPager 를 연결하여 스와이프 탭 다 가능하게함
        new TabLayoutMediator(
                bind.joinTab,
                bind.joinViewpager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        String[] data = {"이메일 인증", "문자 인증"};
                        tab.setText(data[position]);
                    }
                }).attach();
    }


}