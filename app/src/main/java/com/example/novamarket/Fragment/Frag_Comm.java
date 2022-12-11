package com.example.novamarket.Fragment;

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

import com.example.novamarket.Activity.Comm_Cate;
import com.example.novamarket.Activity.Comm_Search;
import com.example.novamarket.Activity.Comm_Write;
import com.example.novamarket.Activity.Home_Cate;
import com.example.novamarket.Adapter.Comm_adapter;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Comm;
import com.example.novamarket.Retrofit.DTO_Home;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.FragmentCommBinding;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Frag_Comm extends Fragment {
    private FragmentCommBinding bind;
    private int page = 1, size = 7;
    private boolean scroll = false;
    private String user_id = null, cate = "", search = "";
    private Comm_adapter adapter;
    private boolean isStop = false;

    public Frag_Comm() {
        // Required empty public constructor
    }


    public static Frag_Comm newInstance() {
        Frag_Comm fragment = new Frag_Comm();
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
        bind = FragmentCommBinding.inflate(inflater,container,false);
        View view = bind.getRoot();

        // 어뎁터 세팅
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false);
        bind.commRv.setLayoutManager(linearLayoutManager);
        adapter = new Comm_adapter(requireContext());
        bind.commRv.setAdapter(adapter);
        bind.commRv.setItemAnimator(null);

        // 글 쓰러 가기
        bind.commWrite.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), Comm_Write.class);
            startActivity(intent);
        });

        /* 새로고침 */
        bind.commSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                search = "";
                cate = "";
                getData();
                bind.commSwipe.setRefreshing(false);
            }
        });

        /* 검색 */
        bind.commSearch.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Comm_Search.class);
            startActivity(intent);
        });

        /* 카테고리 */
        bind.commCate.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), Comm_Cate.class);
            startActivity(intent);
        });

        /* 스크롤 페이징 */
        bind.commRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (adapter.getItemCount() > 0) { // 멘처음 자동 작동 방지
                    if (!bind.commRv.canScrollVertically(1)) {
                        if (!scroll) {
                            scroll = true;
                            getScroll();
                        }
                    }
                }
            }
        });
        getData();

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        isStop = true;
        page = 1;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isStop){
            page = 1;
            getData();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bind = null;
    }

    private void getData() {
        if (page == 1) {
            adapter.clearAll();
        }
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<ArrayList<DTO_Comm>> call = retrofitInterface.getComm(page,size,search,cate);
        call.enqueue(new Callback<ArrayList<DTO_Comm>>() {
            @Override
            public void onResponse(Call<ArrayList<DTO_Comm>> call, Response<ArrayList<DTO_Comm>> response) {
                if(response.isSuccessful() && response.body() != null){
                    for (DTO_Comm item : response.body()) {
                        adapter.addItems(item);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_Comm>> call, Throwable t) {

            }
        });
    }

    private void getScroll(){
        if (page == 1) {
            page = 2;
        }

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<ArrayList<DTO_Comm>> call = retrofitInterface.getComm(page,size,search,cate);
        call.enqueue(new Callback<ArrayList<DTO_Comm>>() {
            @Override
            public void onResponse(Call<ArrayList<DTO_Comm>> call, Response<ArrayList<DTO_Comm>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().size() > 0) {
                        page++;
                    }
                    for (DTO_Comm item : response.body()) {
                        adapter.addItems(item);
                    }
                    scroll = false;
                }
            }

            @Override
            public void onFailure(Call<ArrayList<DTO_Comm>> call, Throwable t) {

            }
        });

    }
}