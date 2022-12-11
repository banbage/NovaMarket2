package com.example.novamarket.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.novamarket.Adapter.ViewPager_adpater;
import com.example.novamarket.Class.GlobalHelper;
import com.example.novamarket.Class.JsonMaker;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.Fragment.Frag_Chat;
import com.example.novamarket.Fragment.Frag_Comm;
import com.example.novamarket.Fragment.Frag_Home;
import com.example.novamarket.Fragment.Frag_Mypage;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.example.novamarket.Retrofit.DTO_user;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.Service.Service_Chat;
import com.example.novamarket.databinding.ActivityHomeBinding;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity {
    private List<String> userInfo = new ArrayList<>();
    private ActivityHomeBinding bind;
    private Frag_Home frag_home;
    private Frag_Comm frag_comm;
    private Frag_Chat frag_chat;
    private Frag_Mypage frag_mypage;
    private ViewPager_adpater vp_adapter;
    private final long FINISH_TIME = 1000;
    private long pressTime = 0;
    private int LoginType;
    private final int TYPE_KAKAO = 0;
    private final int TYPE_NAVER = 1;
    private final int TYPE_GOOGLE = 2;
    private final int TYPE_LOGIN = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        /* 유저 정보 가져오기 */
        initView();
        createFramgent();
        createViewPager();

        // 소켓 서비스 시작
        String user_id = PreferenceManager.getString(Home.this,"user_id");
        Intent service = new Intent(this, Service_Chat.class);
        service.putExtra("user_id", user_id);
        startService(service);

        // 동적 리시버
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(msgHomeReceiver, new IntentFilter("Act_Home"));



        // 네비게이션
        bind.homeNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_tab:
                    bind.homeViewPager.setCurrentItem(0);
                    return true;
                case R.id.community_tab:
                    bind.homeViewPager.setCurrentItem(1);
                    return true;
                case R.id.chat_tab:
                    bind.homeViewPager.setCurrentItem(2);
                    return true;
                case R.id.myPage_tab:
                    bind.homeViewPager.setCurrentItem(3);
                    return true;
            }
            return false;
        });
    }
    private BroadcastReceiver msgHomeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receive = intent.getStringExtra("msg");
            DTO_Chat message = JsonMaker.makeDTO(receive);

            /* 현재 chat fragment 를 보고 있으면 메세지를 전달해 줌  */
            Intent intent1 = new Intent("Frag_Chat");
            intent1.putExtra("msg", receive);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
        }
    };

    private void createFramgent() {
        frag_home = Frag_Home.newInstance();
        frag_comm = Frag_Comm.newInstance();
        frag_chat = Frag_Chat.newInstance();
        frag_mypage = Frag_Mypage.newInstance();
    }

    private void createViewPager() {
        vp_adapter = new ViewPager_adpater(this);
        vp_adapter.addItem(frag_home);
        vp_adapter.addItem(frag_comm);
        vp_adapter.addItem(frag_chat);
        vp_adapter.addItem(frag_mypage);
        bind.homeViewPager.setAdapter(vp_adapter);
        bind.homeViewPager.setUserInputEnabled(false);
    }

    private void initView() {
        LoginType = PreferenceManager.getInt(getBaseContext(), "loginType");
        GlobalHelper mGlobalHelper = new GlobalHelper();
        Logger.d("로그인 TYPE : " + LoginType);


        switch (LoginType) {
            case TYPE_KAKAO:
                userInfo = GlobalHelper.getGlobalUserLoginInfo();
                PreferenceManager.setString(this, "user_profile", userInfo.get(0));
                PreferenceManager.setString(this, "user_name", userInfo.get(1));
                PreferenceManager.setString(this, "user_id", userInfo.get(2));
                joinUser(userInfo.get(2),userInfo.get(1),userInfo.get(0));
                Logger.d("Profile : " + userInfo.get(0));
                Logger.d("Nickname : " + userInfo.get(1));
                Logger.d("userId : " + userInfo.get(2));
                break;
            case TYPE_NAVER:
                userInfo = GlobalHelper.getGlobalUserLoginInfo();
                PreferenceManager.setString(this, "user_profile", userInfo.get(0));
                PreferenceManager.setString(this, "user_name", userInfo.get(1));
                PreferenceManager.setString(this, "user_id", userInfo.get(2));
                joinUser(userInfo.get(2),userInfo.get(1),userInfo.get(0));
                Logger.d("Profile : " + userInfo.get(0));
                Logger.d("Nickname : " + userInfo.get(1));
                Logger.d("userId : " + userInfo.get(2));
                Logger.d("네이버 로그인 정보 : " + userInfo.toString());

                break;
            case TYPE_GOOGLE:
                Logger.d("구글");
                break;
            case TYPE_LOGIN:
                Intent intent = getIntent();
                String profile = PreferenceManager.getString(getBaseContext(), "user_profile");
                String Name = PreferenceManager.getString(getBaseContext(), "user_id");
                String ID = PreferenceManager.getString(getBaseContext(), "user_name");

                Logger.d("Profile : " + profile);
                Logger.d("Nickname : " + Name);
                Logger.d("userId : " + ID);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgHomeReceiver);
    }

    private void joinUser(String ID, String Name, String Profile) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.writeAPI(ID,Name,Profile);
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                if (response.body() != null && response.isSuccessful()){

                }
            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                Logger.d("에러 코드 : " + t);
            }
        });
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - pressTime;

        if (0 <= intervalTime && FINISH_TIME >= intervalTime) {
            finish();
        } else {
            int pos = bind.homeNavigation.getSelectedItemId();
            if (pos != R.id.home_tab) {
                bind.homeViewPager.setCurrentItem(0);
                bind.homeNavigation.setSelectedItemId(R.id.home_tab);
            } else {
                pressTime = tempTime;
                Toast.makeText(this, "한번 더 누르면 앱이 종료됩니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}