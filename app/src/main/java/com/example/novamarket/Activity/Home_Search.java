package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.novamarket.Adapter.Search_adapter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.CheckEvent;
import com.example.novamarket.EventBus.SearchEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Search;
import com.example.novamarket.databinding.ActivityHomeSearchBinding;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class Home_Search extends AppCompatActivity {
    private ActivityHomeSearchBinding bind;
    public Search_adapter adapter;
    private String user_id;
    private boolean checked;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeSearchBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        // 최신 검색어 동작 로직
        // 1. 검색어를 입력했는지 확인 -> true 2번 진행
        // 2. 검색어를 저장하는지 아닌지 확인 ->  true 3번 진행
        // 3. 검색어가 중복됬는지 아닌지 확인 -> true 검색어 어뎁터에 추가
        // 4. onStop 에서 최종 목록을 서버에 다시 저장 (전부 지우고, 다시 저장) 유저 Index 와 dataList 를 서버로 올림

        // 검색어 Shared 에 저장 불러오기
        String str = PreferenceManager.getString(getBaseContext(), "search");
        checked = PreferenceManager.getBoolean(getBaseContext(), "isChecked");

        gson = new Gson();
        ArrayList<String> search = gson.fromJson(str, ArrayList.class);

        // 어뎁터 세팅
        adapter = new Search_adapter(getBaseContext());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getBaseContext(), 2, GridLayoutManager.VERTICAL,false);
        bind.homeSearchRv.setLayoutManager(gridLayoutManager);
        bind.homeSearchRv.setAdapter(adapter);
        if(search != null){
            for(String item : search){
                adapter.addItem(new DTO_Search(item));
            }
        }

        // switch 버튼 처리
        bind.homeSearchSwitch.setChecked(checked);
        bind.homeSearchSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checked = true;
            } else {
                checked = false;
            }
        });

        // 모두 지우기
        bind.homeSearchClear.setOnClickListener(v -> {
            adapter.clearItems();
        });

        // 검색어 입력 버튼을 눌럿을 때 -> 검색어 입력한 값이 있으면 검색 페이지로 결과를 콜백함
        bind.homeSearchBtn.setOnClickListener(v -> {
            SearchBtn();
        });


    }

    private void SearchBtn() {
        // 여백 제거
        String content = bind.homeSearchContent.getText().toString().trim();

        // 유효성 체크
        if (content.isEmpty()) {
            bind.homeSearchContent.setError("검색어를 입력해주세요");
            bind.homeSearchContent.requestFocus();
            return;
        }

        // 저장 안할경우
        if (!checked) {
            EventBus.getDefault().post(new SearchEvent(content,"search"));
            finish();
            return;
        }

        // 중복 체크
        if (!adapter.getContents().contains(content)) {
            adapter.addItem(new DTO_Search(content));
        }

        // 콜백 요청 결과 보내기
        EventBus.getDefault().post(new SearchEvent(content,"search"));
        finish();
        Log.e("콜백 값 가져오기", "콜백 값 : " + content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

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

        gson = new Gson();
        String stringArr = gson.toJson(adapter.getContents());
        PreferenceManager.setString(getBaseContext(), "search", stringArr);
        PreferenceManager.setBoolean(getBaseContext(), "isChecked", checked);
    }
}