package com.example.novamarket.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.novamarket.Adapter.Pager_Adapter;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.JsonMaker;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.CheckEvent;
import com.example.novamarket.EventBus.StateEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.example.novamarket.Retrofit.DTO_Home;
import com.example.novamarket.Retrofit.DTO_Image;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.Service.Send;
import com.example.novamarket.Service.Service_Chat;
import com.example.novamarket.databinding.ActivityHomeReadBinding;
import com.kakao.util.helper.log.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.Socket;
import java.text.DecimalFormat;
import java.text.ParseException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_Read extends AppCompatActivity {
    ActivityHomeReadBinding bind;
    private String user_id;
    private String home_id, read_id;
    private String writer;
    private Pager_Adapter pager_adapter;
    private Context context = this;
    private boolean check = false;
    private boolean like = false;
    private int ChatList = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeReadBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {

        }

        // 초기값 세팅
        context = this;
        Intent intent = getIntent();
        home_id = intent.getStringExtra("home_id");
        user_id = PreferenceManager.getString(this, "user_id");


        // 어뎁터 세팅
        pager_adapter = new Pager_Adapter(getBaseContext());
        bind.homeReadVp.setAdapter(pager_adapter);
        bind.homeReadVp.setOffscreenPageLimit(3);
        bind.homeReadVp.setPageTransformer(new MarginPageTransformer(100));
        bind.homeReadIndicator.setViewPager(bind.homeReadVp);

        // 인디케이터 세팅
        bind.homeReadVp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bind.homeReadIndicator.animatePageSelected(position);
            }
        });

        // 판매 상태 변경
        bind.homeReadState.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, Slide.class);
            startActivity(intent1);
        });

        // 메뉴 -> 수정, 삭제
        bind.homeReadMenu.setOnClickListener(v -> {
            setDialog();
        });

        // 채팅하기, 채팅목록
        // 1. 채팅하기(구매자 시점) => 채팅하기 클릭시 채팅방생성 => 채팅방 입장
        // 2. 채팅목록(판매자 시점) => 채팅목록 엑티비티로 이동 => 클릭시 채팅방 입장
        bind.homeReadChat.setOnClickListener(v -> {
            String txt = bind.homeReadChat.getText().toString();
            switch (txt) {
                case "채팅하기": // 채팅 만들기
                    doChat();
                    break;
                default:
                    doChatList();
                    break;
            }
        });

        // 좋아요 클릭
        bind.homeReadLike.setOnClickListener(v -> {
            if (like) {
                like = false;
                bind.homeReadLike.setImageResource(R.drawable.heartwhite);
                bind.homeReadLikeNumber.setVisibility(View.GONE);
                deleteLike();

            } else {
                like = true;
                bind.homeReadLike.setImageResource(R.drawable.heartblack);
                bind.homeReadLikeNumber.setVisibility(View.VISIBLE);
                writeLike();

            }
        });

        getData(false);
    }

    private void doChat() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Chat> call = retrofitInterface.writeChat(home_id,read_id,user_id); // 어떤글에서 만들어진 채팅방인지, 어떤 유저가 채팅을 요청했는지
        call.enqueue(new Callback<DTO_Chat>() {
            @Override
            public void onResponse(Call<DTO_Chat> call, Response<DTO_Chat> response) {
                if(response.isSuccessful() && response.body() != null){
                    String room_id = response.body().getRoom_id();

                    Socket socket = Service_Chat.getSocket();
                    String msg = JsonMaker.makeJson(new DTO_Chat("", room_id, user_id, read_id, "join","", "", "", "", "", "", "", home_id));
                    Send send = new Send(socket, msg);
                    send.start();

                    socket = null;

                    Intent intent = new Intent(Home_Read.this, Home_Chat.class);
                    intent.putExtra("home_id", home_id);
                    intent.putExtra("read_id", read_id);
                    intent.putExtra("room_id", room_id);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<DTO_Chat> call, Throwable t) {

            }
        });


    }

    private void doChatList() {
        Intent intent = new Intent(this,Home_Chat_list.class);
        intent.putExtra("home_id", home_id);
        startActivity(intent);
    }

    private void deleteLike() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.deleteLike(home_id, user_id, "home");
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                if (response.body() != null && response.isSuccessful()) {
                    // 좋아요 수
                    if (response.body().getLike() > 0) {
                        bind.homeReadLikeNumber.setVisibility(View.VISIBLE);
                        bind.homeReadLikeNumber.setText("관심 " + response.body().getLike());
                    } else {
                        bind.homeReadLikeNumber.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {

            }
        });
    }

    private void writeLike() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.writeLike(home_id, user_id, "home");
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 좋아요 수
                    if (response.body().getLike() > 0) {
                        bind.homeReadLikeNumber.setVisibility(View.VISIBLE);
                        bind.homeReadLikeNumber.setText("관심 " + response.body().getLike());
                    } else {
                        bind.homeReadLikeNumber.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {

            }
        });
    }

    private void setDialog() {
        Dialog dialog = new Dialog(Home_Read.this);
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
            HomeUpdate();
        });

        // 게시글 삭제
        btn3.setOnClickListener(v -> {
            dialog.dismiss();
            HomeDelete();
        });
    }

    private void HomeDelete() {
        Dialog dialog = new Dialog(Home_Read.this);
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
            Call<DTO_Home> call = retrofitInterface.deleteHome(user_id, home_id);
            call.enqueue(new Callback<DTO_Home>() {
                @Override
                public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        finish();
                        Logger.d("삭제");
                    }
                }

                @Override
                public void onFailure(Call<DTO_Home> call, Throwable t) {
                    Logger.d("에러 메세지 : " + t);
                    finish();
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

    @Override
    protected void onRestart() {
        super.onRestart();
        getData(true);
        getData(true);
    }

    private void HomeUpdate() {
        Intent intent = new Intent(Home_Read.this, Home_Write.class);
        intent.putExtra("home_id", home_id);
        startActivity(intent);
    }

    private void setState(String callback) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.updateState(home_id, callback);
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                if (response.isSuccessful() && response.body() != null) {

                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getData(boolean check) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.getHomeRead(home_id, user_id);
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {

                // 이미지 배치
                if (check) {
                    pager_adapter.clearItem();
                }

                if (response.isSuccessful() && response.body() != null) {
                    writer = response.body().getUser_id(); // 글 작성자 정보 가져오기
                    for (DTO_Image url : response.body().getImages()) {
                        pager_adapter.addItem(url.getImage());
                    }
                    read_id = response.body().getUser_id();

                    Logger.d(read_id);

                    bind.homeReadIndicator.createIndicators(pager_adapter.getItemCount(), 0);
                    // 프로필 세팅
                    Glide.with(context).load(response.body().getUser_profile()).circleCrop().into(bind.homeUserProfile);
                    bind.homeUserName.setText(response.body().getUser_name());
                    // 글쓴이 확인, 메뉴, State 창 보임, 채팅목록 보기
                    if (user_id.equals(response.body().getUser_id())) {
                        bind.homeReadMenu.setVisibility(View.VISIBLE);
                        bind.homeReadState.setVisibility(View.VISIBLE);
                        if(response.body().getChat() > 0) {
                            bind.homeReadChat.setText("채팅목록(" + response.body().getChat() + ")");
                        }else{
                            bind.homeReadChat.setText("채팅목록");
                        }
                    }

                    // 게시판 값 세팅
                    bind.homeReadTitle.setText(response.body().getTitle());
                    bind.homeReadContent.setText(response.body().getContent());
                    bind.homeReadCate.setText(response.body().getCate());
                    DecimalFormat decimalFormat = new DecimalFormat("###,###");
                    String number = decimalFormat.format(response.body().getPrice());
                    bind.homeReadPrice.setText(number + "원");
                    try {
                        bind.homeReadDate.setText(DateConverter.resultDateToString(response.body().getDate(), "M월 d일 a h시 m분"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    // 좋아요 여부
                    if (response.body().isLike_check()) {
                        bind.homeReadLike.setImageResource(R.drawable.heartblack);
                        like = true;
                    } else {
                        bind.homeReadLike.setImageResource(R.drawable.heartwhite);
                        like = false;
                    }
                    // 좋아요 수
                    if (response.body().getLike() > 0) {
                        bind.homeReadLikeNumber.setVisibility(View.VISIBLE);
                        bind.homeReadLikeNumber.setText("관심 " + response.body().getLike());
                    } else {
                        bind.homeReadLikeNumber.setVisibility(View.GONE);
                    }
                    // 채팅 수
                    if (response.body().getChat() > 0) {
                        bind.homeReadChatNumber.setVisibility(View.VISIBLE);
                        bind.homeReadChatNumber.setText("채팅 " + response.body().getChat());
                    } else {
                        bind.homeReadChatNumber.setVisibility(View.GONE);
                    }
                    // 판매 상태
                    bind.homeReadStateTxt.setText(response.body().getState());
                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {
                Logger.d("에러 메세지 : " + t.toString());
            }
        });
    }

    // 이벤트를 받음
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void callEvent(StateEvent event) {
        String content = event.getState();
        switch (content) {
            case "예약중":
                bind.homeReadStateTxt.setText(content);
                setState(content);
                break;
            case "판매완료":
                bind.homeReadStateTxt.setText(content);
                setState(content);
                break;
            case "판매중":
                bind.homeReadStateTxt.setText(content);
                setState(content);
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void callWrite(CheckEvent event) {
        if (event.check) {
            getData(event.check);

        }
    }
}