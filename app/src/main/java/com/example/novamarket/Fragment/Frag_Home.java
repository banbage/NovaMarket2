package com.example.novamarket.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.novamarket.Activity.Home_Cate;
import com.example.novamarket.Activity.Home_Search;
import com.example.novamarket.Activity.Home_Write;
import com.example.novamarket.Adapter.Home_adapter;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.SearchEvent;
import com.example.novamarket.Retrofit.DTO_Home;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.FragmentHomeBinding;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Frag_Home extends Fragment {
    private FragmentHomeBinding bind;
    private int page = 1, size = 7;
    private boolean scroll = false;
    private String user_id = null, cate = "", search = "";
    private Home_adapter adapter;
    private boolean isStop = false;


    public static Frag_Home newInstance() {
        Frag_Home fragment = new Frag_Home();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void searchEvent(SearchEvent event) {
        isStop = false;
        if (event.type.equals("search")) {
            search = event.content;
            adapter.clearAll();
            getData();
        } else if (event.type.equals("cate")) {
            cate = event.content;
            bind.homeTitle.setText(cate);
            if (cate.equals("전체")) {
                cate = "";
                Logger.d("전체라 초기화");
            }
            adapter.clearAll();
            getData();
            Logger.d(cate);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        bind = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isStop) {
            page = 1;
            getData();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        isStop = true;
        page = 1;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void getScroll() {
        if (page == 1) {
            page = 2;
        }

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<ArrayList<DTO_Home>> call = retrofitInterface.getHome(page, size, search, cate);
        call.enqueue(new Callback<ArrayList<DTO_Home>>() {
            @Override
            public void onResponse(Call<ArrayList<DTO_Home>> call, Response<ArrayList<DTO_Home>> response) {
                Logger.json(GsonConverter.setLog(response.body()));
                if (response.body() != null && response.isSuccessful()) {
                    if (response.body().size() > 0) {
                        page++;
                    }
                    for (DTO_Home item : response.body()) {
                        adapter.addItem(item);
                    }
                    scroll = false;
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_Home>> call, Throwable t) {
                Logger.e("에러메세지 : " + t);
            }
        });
    }

    private void getData() {
        if (page == 1) {
            adapter.clearAll();
        }
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<ArrayList<DTO_Home>> call = retrofitInterface.getHome(page, size, search, cate);
        call.enqueue(new Callback<ArrayList<DTO_Home>>() {
            @Override
            public void onResponse(Call<ArrayList<DTO_Home>> call, Response<ArrayList<DTO_Home>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    for (DTO_Home item : response.body()) {
                        adapter.addItem(item);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_Home>> call, Throwable t) {
                Logger.e("에러코드 : " + t);
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        bind = FragmentHomeBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        user_id = PreferenceManager.getString(requireContext(), "user_id");


        // 어뎁터 세팅
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        bind.homeRv.setLayoutManager(linearLayoutManager);
        adapter = new Home_adapter(requireContext());
        bind.homeRv.setAdapter(adapter);
        bind.homeRv.setItemAnimator(null);

        // 검색
        bind.homeSearch.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Home_Search.class);
            startActivity(intent);
        });

        // 카테고리
        bind.homeCate.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Home_Cate.class);
            startActivity(intent);
        });

        // 새 글쓰러가기
        bind.homeWrite.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Home_Write.class);
            intent.putExtra("type", false);
            startActivity(intent);
        });


        /* 스크롤 페이징 */
        bind.homeRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (adapter.getItemCount() > 0) { // 멘처음 자동 작동 방지
                    if (!bind.homeRv.canScrollVertically(1)) {
                        if (!scroll) {
                            scroll = true;
                            getScroll();
                        }
                    }
                }
            }
        });

        // 새로고침
        bind.homeSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                search = "";
                cate = "";
                getData();
                bind.homeSwipe.setRefreshing(false);
            }
        });

        getData();

        return view;
    }
}