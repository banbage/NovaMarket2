package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.novamarket.Adapter.Chat_list_adapter;
import com.example.novamarket.Class.JsonMaker;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityHomeChatListBinding;
import com.orhanobut.logger.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_Chat_list extends AppCompatActivity {
    private ActivityHomeChatListBinding bind;
    private int page = 1, size = 8;
    private String user_id, home_id;
    private Chat_list_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeChatListBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Intent intent = getIntent();
        home_id = intent.getStringExtra("home_id");
        user_id = PreferenceManager.getString(getBaseContext(), "user_id");
        Logger.d(user_id);

        // 어뎁터 세팅
        adapter = new Chat_list_adapter(Home_Chat_list.this);
        bind.chatListRv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Home_Chat_list.this, LinearLayoutManager.VERTICAL, false);
        bind.chatListRv.setLayoutManager(linearLayoutManager);
        bind.chatListRv.setItemAnimator(null);
        adapter.setHome(home_id);

        // Swipe 새로고침
        bind.chatListSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getData(true, false);
                bind.chatListSwipe.setRefreshing(false);
            }
        });

        // Scroll
        bind.chatListScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    getData(false,true);
                }
            }
        });

        getData(false, false);

        /* 동적 broadCastReceive 등록 */

        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, new IntentFilter("Act_List"));
    }

    private BroadcastReceiver msgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            DTO_Chat message = JsonMaker.makeDTO(msg);
            String code = message.getMsg_code();
            String id = message.getUser_id();
            String content = message.getMsg();
            String room = message.getRoom_id();

            if(code.equals("send") && !user_id.equals(id)){
                page =1;
                getData(true,false);
            }
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        getData(true,false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* 등록 해제 잊지말고 하기 안하면 중복 에러 발생 */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
    }

    private void getData(boolean clear, boolean scroll) {
        if(clear){
            adapter.clearItems();
        }
        if(scroll){
            page++;
        }
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Chat> call = retrofitInterface.getChatList(home_id, user_id, page, size);
        call.enqueue(new Callback<DTO_Chat>() {
            @Override
            public void onResponse(Call<DTO_Chat> call, Response<DTO_Chat> response) {
                if (response.body() != null && response.isSuccessful()) {
                    for(DTO_Chat item : response.body().getMessages()){
                        adapter.addItem(item);
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_Chat> call, Throwable t) {
                Logger.d("에러메세지");
            }
        });
    }
}