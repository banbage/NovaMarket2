package com.example.novamarket.Activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.example.novamarket.Adapter.Reply_Adapter;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Comment;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityCommReplyBinding;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.text.ParseException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comm_Reply extends AppCompatActivity {
    private ActivityCommReplyBinding bind;
    private String comm_id = null, comment_id = null, user_id = null, writer = null;
    private int page = 1, size = 8;
    private Context context = this;
    private Reply_Adapter adapter;
    private boolean scroll = false;
    private boolean check = false;
    private boolean touch = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityCommReplyBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        comm_id = getIntent().getStringExtra("comm_id");
        comment_id = getIntent().getStringExtra("comment_id");
        writer = getIntent().getStringExtra("writer");
        user_id = getIntent().getStringExtra("user_id");

        adapter = new Reply_Adapter(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        bind.replyRv.setLayoutManager(linearLayoutManager);
        bind.replyRv.setAdapter(adapter);
        bind.replyRv.setItemAnimator(null);
        adapter.setWriter(writer);

        if (!writer.equals(user_id)) {
            bind.replyWriter.setVisibility(View.GONE);
        } else {
            bind.replyRv.setVisibility(View.VISIBLE);
        }
        user_id = PreferenceManager.getString(getBaseContext(), "user_id");


        // 댓글 작성하려고 하면 입력버튼 보이게하기
        bind.replyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 현재 입력되어있는 글자 갯수
                int length = bind.replyEt.getText().toString().length();
                if (length == 0) {
                    bind.replyWrite.setVisibility(View.GONE);
                } else {
                    bind.replyWrite.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 댓글 작성하기
        bind.replyWrite.setOnClickListener(v -> {
            writeReply();
            keyboardDown();
        });

        // 스크롤 댓글 불러오기
        bind.replyScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    getScroll();
                }
            }
        });


        // 댓글 가져오기
        getData(false);
    }

    private void getScroll() {
        page++;
        // 댓글 페이징시 이미지는 삭제해야됨
        getData(false);
    }

    private void writeReply() {
        // 댓글 작성시 기존의 작성한 내용 없애기
        String content = bind.replyEt.getText().toString();
        bind.replyEt.setText(null);

        // 답글 업로드 및 adpater 에 저장된 내용 작성하기
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Comment> call = retrofitInterface.writeReply(comm_id, comment_id, user_id, content);
        call.enqueue(new Callback<DTO_Comment>() {
            @Override
            public void onResponse(Call<DTO_Comment> call, Response<DTO_Comment> response) {
                Logger.json(GsonConverter.setLog(response.body()));
                if (response.isSuccessful() && response.body() != null) {
                    // 댓글 추가
                    adapter.addItem(response.body());
                    // 페이징 기준으로 댓글 스크롤 여부 결정
                    if (adapter.getItemCount() < size) { // 댓글이 페이징 처리 갯수를 넘어서지 않았거나, 하단 바를 터지했을경우 댓글을 불러온다.
                        bind.replyScroll.fullScroll(View.FOCUS_DOWN);
                    }else if (touch){
                        bind.replyScroll.fullScroll(View.FOCUS_DOWN);
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_Comment> call, Throwable t) {
                Logger.d("에러 메세지 : " + t.getMessage());
            }
        });


    }

    private void getData(boolean reply) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Comment> call = retrofitInterface.getReply(comm_id, comment_id, user_id, page, size);
        call.enqueue(new Callback<DTO_Comment>() {
            @Override
            public void onResponse(Call<DTO_Comment> call, Response<DTO_Comment> response) {
                if (response.body() != null && response.isSuccessful()) {
                    if (reply) {
                        adapter.clearItems();
                    }

                    if (response.body().getReplies().size() == 0){
                        touch = true;
                    }

                    // 프로필 설정
                    bind.replyName.setText(response.body().getName());
                    Glide.with(context).load(response.body().getUser_profile()).circleCrop().into(bind.replyImage);
                    bind.replyContent.setText(response.body().getContent());
                    try {
                        bind.replyDate.setText(DateConverter.getUploadMinuteTime(response.body().getDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // 좋아요 체크
                    if (response.body().getLike() != 0) {
                        bind.replyLike.setText("좋아요" + response.body().getLike());
                    } else {
                        bind.replyLike.setText("좋아요");
                    }

                    for (DTO_Comment item : response.body().getReplies()) {
                        adapter.addItem(item);
                    }

                    scroll = false;
                }
            }

            @Override
            public void onFailure(Call<DTO_Comment> call, Throwable t) {
                Logger.d("에러메세지 : " + t.getMessage());
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            // 사각형을 만드는 클래스 (Rectangle) 직사각형
            Rect rect = new Rect();
            // RootView 레이아웃을 기준으로 한 좌표 => 가장 바깥을 감싸는 Viewgroup => Constraint, Liner등등 알지?
            focusView.getGlobalVisibleRect(rect);
            // 현재 이벤트가 일어난 x, y 좌표를 가져옴
            int x = (int) ev.getX(), y = (int) ev.getY();
            // 클릭이벤트가 focusView 범위 안에 일어났는지 확인
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void keyboardDown() {
        // 키보드 내리기
        View view = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }
}