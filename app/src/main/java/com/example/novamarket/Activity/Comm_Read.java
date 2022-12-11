package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.novamarket.Adapter.Comment_Adapter;
import com.example.novamarket.Adapter.Pager_Adapter;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.CheckEvent;
import com.example.novamarket.EventBus.ReplyEvent;
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Comm;
import com.example.novamarket.Retrofit.DTO_Comment;
import com.example.novamarket.Retrofit.DTO_Home;
import com.example.novamarket.Retrofit.DTO_Image;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityCommReadBinding;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comm_Read extends AppCompatActivity {
    ActivityCommReadBinding bind;
    private String user_id;
    private String comm_id;
    private String writer;
    private Pager_Adapter pager_adapter;
    private Context context = this;
    private boolean check = false;
    private boolean like = false; // 게시글 좋아요 표시
    private boolean touch = false; // 현재 최하단에 맞닿아있는지 아닌지 확인
    private int page = 1, size = 8; // 페이징 처리
    private Comment_Adapter comment_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityCommReadBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());


        // 초기값 세팅
        Intent intent = getIntent();
        comm_id = intent.getStringExtra("comm_id");
        user_id = PreferenceManager.getString(this, "user_id");

        // 어뎁터 설정
        // 이미지 어뎁터
        pager_adapter = new Pager_Adapter(getBaseContext());
        bind.commReadVp.setAdapter(pager_adapter);
        bind.commReadVp.setOffscreenPageLimit(3);
        bind.commReadVp.setPageTransformer(new MarginPageTransformer(100));
        bind.commReadIndicator.setViewPager(bind.commReadVp);
        // 댓글 어뎁터
        comment_adapter = new Comment_Adapter(getBaseContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.VERTICAL, false);
        bind.commReadCommentRv.setAdapter(comment_adapter);
        bind.commReadCommentRv.setLayoutManager(linearLayoutManager);
        bind.commReadCommentRv.setItemAnimator(null);


        // 댓글 작성하려고 하면 입력버튼 보이게하기
        bind.commReadCommentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 현재 입력되어있는 글자 갯수
                int length = bind.commReadCommentEt.getText().toString().length();
                if (length == 0) {
                    bind.commReadCommentWrite.setVisibility(View.GONE);
                } else {
                    bind.commReadCommentWrite.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 인디케이터 세팅
        bind.commReadVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bind.commReadIndicator.animatePageSelected(position);
            }
        });

        // 메뉴 -> 수정, 삭제
        bind.commReadMenu.setOnClickListener(v -> {
            setDialog();
        });

        bind.commReadSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                touch = false;
                getData(true, true);
                bind.commReadSwipe.setRefreshing(false);
            }
        });

        // 좋아요 클릭
        bind.itemCommLikeLayout.setOnClickListener(v -> {
            if (like) {
                like = false;
                bind.itemCommLike.setImageResource(R.drawable.heartwhite);
                deleteLike();
                Logger.d("좋아요 삭제");
            } else {
                like = true;
                bind.itemCommLike.setImageResource(R.drawable.heartblack);
                writeLike();
                Logger.d("좋아요 등록");
            }
        });

        // 스크롤 댓글 불러오기
        bind.commReadScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                    getScroll();
                }
            }
        });

        // 댓글 작성하기
        bind.commReadCommentWrite.setOnClickListener(v -> {
            writeComment();
            keyboardDown();
        });

        // check = 이미지, 답글 어뎁터 초기화 여부
        // 게시글 초기 세팅하기
        getData(false, false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        page = 1;
        touch = false;
        getData(true, true);
    }

    private void getScroll() {
        if (page == 1) {
            page = 2;
        } else {
            page++;
        }
        // 댓글 페이징시 이미지는 삭제해야됨
        getData(false, true);
    }

    private void writeReply() {
    }

    private void writeComment() {
        // 댓글 작성시 기존에 작성한 내용 없애기
        String content = bind.commReadCommentEt.getText().toString();

        bind.commReadCommentEt.setText(null);

        // 댓글 업로드 및 adapter 에 저장된 내용 작성하기
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Comment> call = retrofitInterface.writeComment(comm_id, user_id, content);
        call.enqueue(new Callback<DTO_Comment>() {
            @Override
            public void onResponse(Call<DTO_Comment> call, Response<DTO_Comment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 댓글 추가
                    comment_adapter.addItem(response.body());
                    // 페이징 기준으로 댓글 스크롤 여부 결정
                    if (comment_adapter.getItemCount() < size || touch) { // 댓글이 페이징 처리 갯수를 넘어서지 않았거나, 하단 바를 터지했을경우 댓글을 불러온다.
                        bind.commReadScroll.fullScroll(View.FOCUS_DOWN);
                    }
                    bind.itemCommChatTxt.setText("댓글" + response.body().getTotal_comment());
                }
            }

            @Override
            public void onFailure(Call<DTO_Comment> call, Throwable t) {

            }
        });

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

    private void getData(boolean comment, boolean image) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Comm> call = retrofitInterface.getCommRead(comm_id, user_id, page, size);
        call.enqueue(new Callback<DTO_Comm>() {
            @Override
            public void onResponse(Call<DTO_Comm> call, Response<DTO_Comm> response) {
//                Logger.json(GsonConverter.setLog(response.body()));

                if (response.body().getComments().size() == 0) {
                    touch = true;
                }
                if (comment) {
                    comment_adapter.clearItem();
                }
                if (image) {
                    pager_adapter.clearItem();
                }

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getImages().size() == 0) {
                        bind.commReadVp.setVisibility(View.GONE);
                    } else {
                        bind.commReadVp.setVisibility(View.VISIBLE);
                        for (DTO_Image url : response.body().getImages()) {
                            pager_adapter.addItem(url.getImage());
                        }
                    }
                    bind.commReadIndicator.createIndicators(pager_adapter.getItemCount(), 0);
                    // 프로필 설정
                    Glide.with(context).load(response.body().getUser_profile()).circleCrop().into(bind.commUserProfile);
                    bind.commUserName.setText(response.body().getUser_name());
                    // 글쓴이 확인, 메뉴 클릭 여부 확인
                    writer = response.body().getUser_id();
                    comment_adapter.setWriter(writer);
                    if (user_id.equals(writer)) {
                        bind.commReadMenu.setVisibility(View.VISIBLE);
                    }

                    // 게시글 값 세팅
                    try {
                        bind.commReadDate.setText(DateConverter.getUploadMinuteTime(response.body().getDate()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    bind.commReadContent.setText(response.body().getContent());
                    // 좋아요 여부
                    if (response.body().isLike_check()) {
                        bind.itemCommLike.setImageResource(R.drawable.heartblack);
                        like = true;
                    } else {
                        bind.itemCommLike.setImageResource(R.drawable.heartwhite);
                        like = false;
                    }
                    // 좋아요수
                    bind.itemCommLikeTxt.setText("좋아요 " + response.body().getLike());
                    bind.itemCommChatTxt.setText("답글 " + response.body().getTotal_comment());

                    if (response.body().getTotal_comment() == 0) {
                        bind.itemCommChatTxt.setText("답글");
                    }
                    if (response.body().getLike() == 0) {
                        bind.itemCommLikeTxt.setText("좋아요");
                    }

                    // 카테고리
                    bind.commReadState.setText(response.body().getCate());

                    // 댓글 불러오기
                    if (response.body().getComments() != null) {
                        for (DTO_Comment comment : response.body().getComments()) {
                            comment_adapter.addItem(comment);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<DTO_Comm> call, Throwable t) {

            }
        });
    }

    private void writeLike() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.writeLike(comm_id, user_id, "comm");
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 좋아요 수
                    bind.itemCommLikeTxt.setText("좋아요 " + response.body().getLike());
                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {

            }
        });
    }

    private void deleteLike() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.deleteLike(comm_id, user_id, "comm");
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                if (response.body() != null && response.isSuccessful()) {
                    bind.itemCommLikeTxt.setText("좋아요 " + response.body().getLike());
                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {
            }
        });
    }

    private void setDialog() {
        Dialog dialog = new Dialog(Comm_Read.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_menu);

        // 창 띄우기
        dialog.show();

        // ViewBinding
        TextView btn1 = dialog.findViewById(R.id.dialog_btn);
        TextView btn2 = dialog.findViewById(R.id.dialog_btn2);
        TextView btn3 = dialog.findViewById(R.id.dialog_btn3);

        btn1.setVisibility(View.GONE);
        btn2.setText("게시글 수정");
        btn3.setText("삭제");

        // 게시글 수정
        btn2.setOnClickListener(v -> {
            dialog.dismiss();
            CommUpdate();
        });

        // 게시글 삭제
        btn3.setOnClickListener(v -> {
            dialog.dismiss();
            CommDelete();
        });
    }

    private void CommDelete() {
        Dialog dialog = new Dialog(Comm_Read.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_check);

        // 창 띄우기
        dialog.show();

        // ViewBinding
        TextView yes = dialog.findViewById(R.id.dialog_btn_yes);
        TextView no = dialog.findViewById(R.id.dialog_btn_no);

        // 게시글 삭제
        yes.setOnClickListener(v -> {
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<DTO_Comm> call = retrofitInterface.deleteComm(comm_id, user_id);
            call.enqueue(new Callback<DTO_Comm>() {
                @Override
                public void onResponse(Call<DTO_Comm> call, Response<DTO_Comm> response) {
                    finish();
                    Logger.d("삭제");
                }

                @Override
                public void onFailure(Call<DTO_Comm> call, Throwable t) {
                    Logger.d("에러 메세지 : " + t);
                }
            });
            dialog.dismiss();
        });

        // 아니요
        no.setOnClickListener(v -> {
            dialog.dismiss();
            Logger.d("삭제");
        });

    }

    private void CommUpdate() {
        Intent intent = new Intent(Comm_Read.this, Comm_Write.class);
        intent.putExtra("comm_id", comm_id);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void check(CheckEvent event) {
        // 현재 화면을 새로고침 하는지 안하는지 설정
        if (event.check) {
            page = 1;
            touch = false;
            getData(true, true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void totalComment(SendEvent event) {
        // 답글수 등록
        if (event.pos == 0) {
            bind.itemCommChatTxt.setText("답글");
        } else {
            bind.itemCommChatTxt.setText("답글 " + event.pos);
        }
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

}