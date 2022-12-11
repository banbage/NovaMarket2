package com.example.novamarket.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.speech.RecognitionListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.novamarket.Activity.Home_Chat;
import com.example.novamarket.Activity.Home_Chat_list;
import com.example.novamarket.Adapter.Chat_join_adapter;
import com.example.novamarket.Adapter.Chat_list_adapter;
import com.example.novamarket.Class.JsonMaker;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.ImageEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.FragmentChatBinding;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Frag_Chat#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Frag_Chat extends Fragment {
    private FragmentChatBinding bind;
    private int page = 1, size = 8;
    private String user_id;
    private Chat_join_adapter adapter;
    private boolean alarm = false;

    // TODO: Rename and change types and number of parameters
    public static Frag_Chat newInstance() {
        Frag_Chat fragment = new Frag_Chat();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bind = FragmentChatBinding.inflate(inflater, container, false);
        View view = bind.getRoot();

        user_id = PreferenceManager.getString(requireContext(), "user_id");



        // 어뎁터 세팅
        adapter = new Chat_join_adapter(requireContext());
        bind.chatJoinRv.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        bind.chatJoinRv.setLayoutManager(linearLayoutManager);
        bind.chatJoinRv.setItemAnimator(null);

        alarm = PreferenceManager.getBoolean(requireContext(), "alarm"); // default = false;

        if (!alarm) {
            bind.chatJoinAlarm.setImageResource(R.drawable.bell);
            PreferenceManager.setBoolean(requireContext(), "alarm", false);
        } else {
            bind.chatJoinAlarm.setImageResource(R.drawable.no_alarm);
            PreferenceManager.setBoolean(requireContext(), "alarm", true);
        }

        // 채팅 전체 알림
        bind.chatJoinAlarm.setOnClickListener(v -> {
            /* 알람 설정하기  */
            if (!alarm) {
                Logger.d(alarm);
                bind.chatJoinAlarm.setImageResource(R.drawable.no_alarm);
                alarm = true;
                PreferenceManager.setBoolean(requireContext(), "alarm", true);
            } else {
                Logger.d(alarm);
                bind.chatJoinAlarm.setImageResource(R.drawable.bell);
                alarm = false;
                PreferenceManager.setBoolean(requireContext(), "alarm", false);
            }

        });

        // Swipe 새로고침
        bind.chatJoinSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getData(true, false);
                bind.chatJoinSwipe.setRefreshing(false);
            }
        });

        // Scroll
        bind.chatJoinScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    getData(false, true);
                }
            }
        });

        getData(false, false);

        /* 동적 broadCastReceive 등록 */
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(msgReceiver, new IntentFilter("Frag_Chat"));
        return view;
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

            if (code.equals("send") && !user_id.equals(id)) {
                page = 1;
                getData(true, false);
            }
        }
    };

    private void getData(boolean clear, boolean scroll) {
        if (clear) {
            adapter.clearItems();
        }
        if (scroll) {
            page++;
        }

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Chat> call = retrofitInterface.getChatJoin(user_id, page, size);
        call.enqueue(new Callback<DTO_Chat>() {
            @Override
            public void onResponse(Call<DTO_Chat> call, Response<DTO_Chat> response) {
                if (response.body() != null && response.isSuccessful()) {
                    for (DTO_Chat item : response.body().getMessages()) {
                        adapter.addItem(item);
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_Chat> call, Throwable t) {
                Logger.d("에러 발생");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        page = 1;
        getData(true, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* viewBind, broadcast 제거 */
        EventBus.getDefault().unregister(this);

        bind = null;
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(msgReceiver);
    }
}