package com.example.novamarket.Activity;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.novamarket.Adapter.Add_adpater;
import com.example.novamarket.Adapter.Chat_adapter;
import com.example.novamarket.Class.DateConverter;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.Class.JsonMaker;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.ImageEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.Service.Send;
import com.example.novamarket.Service.Service_Chat;
import com.example.novamarket.databinding.ActivityHomeChatBinding;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_Chat extends AppCompatActivity {
    private ActivityHomeChatBinding bind;
    private String home_id, user_id, read_id, user_profile, user_name, msg_date, room_id, state;
    private Context context = this;
    private boolean delete = false, add = false, alarm = false, in = false, scroll = false;
    private int page = 1, size = 15, online = 1;
    private Socket socket;
    private List<Uri> Selected;
    private Chat_adapter chat_adapter;
    private Add_adpater add_adpater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeChatBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        EventBus.getDefault().register(this);

        socket = Service_Chat.getSocket();

        Intent intent = getIntent();
        home_id = intent.getStringExtra("home_id"); // 매물 id
        read_id = intent.getStringExtra("read_id"); // 클라이언트 id
        room_id = intent.getStringExtra("room_id"); // 방정보
        user_id = PreferenceManager.getString(context, "user_id");

        /* 노티 있으면 종료 시키기 */
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        /* 동적 broadCastReceive 등록 */
        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, new IntentFilter("Act_Chat"));

        // 매물정보 클릭시 매물 Acitivity로 이동
        bind.chatHomeItem.setOnClickListener(v -> {
            Intent intent1 = new Intent(Home_Chat.this, Home_Read.class);
            intent1.putExtra("home_id", home_id);
            startActivity(intent1);
        });

        // 메세지 전송
        bind.chatSend.setOnClickListener(v -> {
            sendMessage();
        });

        // 메뉴 클릭
        bind.chatMenu.setOnClickListener(v -> {
            doMenu();
        });

        // 추가 기능 클릭시
        bind.chatAddBtn.setOnClickListener(v -> {
            doAdd();
        });

        // 알림 설정 클릭
        bind.chatAlarm.setOnClickListener(v -> {
            setAlarm();
        });

        // 리사이클러뷰 세팅
        chat_adapter = new Chat_adapter(context);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, RecyclerView.VERTICAL, false);
        bind.chatRv.setAdapter(chat_adapter);
        bind.chatRv.setLayoutManager(linearLayoutManager);
        bind.chatRv.setItemAnimator(null);
        chat_adapter.setHomeChat(Home_Chat.this);

        // 추가 이벤트 어뎁터
        add_adpater = new Add_adpater(context);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4, RecyclerView.VERTICAL, false);
        bind.chatAdd.setAdapter(add_adpater);
        bind.chatAdd.setLayoutManager(gridLayoutManager);
        add_adpater.addItem(new DTO_Chat("이미지", R.drawable.image));
        add_adpater.addItem(new DTO_Chat("장소", R.drawable.locationpin));

        /* 리사이클러뷰 스크롤시 */
        bind.chatRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                /* 최상단 스크롤시 작동 */
                if (!bind.chatRv.canScrollVertically(-1)) {
                    if (!scroll) {
                        scroll = true;
                        getScroll();
                    }
                }
            }
        });

        getData(false);
    }

    // 이벤트 받음
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void ImageEvent(ImageEvent event) {
        String txt = event.where;
        switch (txt) {
            case "이미지":
                // 이미지 넘어가고 보내기 작성하는거지
                Matisse.from(Home_Chat.this)
                        .choose(MimeType.ofAll())
                        .countable(true)
                        .maxSelectable(3)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(1f)
                        .imageEngine(new GlideEngine())
                        .showPreview(false)
                        .forResult(1111);
                break;
            case "장소":
                Intent intent = new Intent(Home_Chat.this, Home_Location.class);
                startActivity(intent);
                break;
            case "장소전송":
                String now = null;
                try {
                    now = DateConverter.setDate();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                bind.chatAdd.setVisibility(View.GONE);
                bind.chatAddBtn.setImageResource(R.drawable.plus);
                add = false;

                /* 서버에 보낼 Json 만들기 */
                DTO_Chat dto =
                        new DTO_Chat(user_profile, room_id, user_id, user_name,
                                "send", "약속장소가 전송되었습니다.", event.url, now, "loc", "" + online, event.lat+"", event.log+"", home_id);
                chat_adapter.addItem(dto);
                String msg = DtoToJson(dto);

                /* Send Thread 로 서버로 보내기 */
                Send send = new Send(socket, msg);
                send.start();
                /* 리사이클러뷰 맨 아래로 이동 */
                bind.chatRv.scrollToPosition(chat_adapter.getItemCount() - 1);
                break;
            default:
        }
    }

    // 절대 경로 가져오기 !!!!!
    private String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(Home_Chat.this, uri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return url;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1111 && resultCode == RESULT_OK) {
            Selected = Matisse.obtainResult(data);
            ArrayList<String> images = new ArrayList<>();
            ArrayList<MultipartBody.Part> files = new ArrayList<>();

            for (Uri uri : Selected) {
                String str = getRealPathFromUri(uri);
                images.add(str);
            }

            /* 이미지 */
            for (int i = 0; i < images.size(); i++) {
                File file = new File(images.get(i));
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-date"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", requestFile);
                files.add(body);
            }

            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<ArrayList<DTO_Chat>> call = retrofitInterface.writerImageMessage(files);
            call.enqueue(new Callback<ArrayList<DTO_Chat>>() {
                @Override
                public void onResponse(Call<ArrayList<DTO_Chat>> call, Response<ArrayList<DTO_Chat>> response) {
                    Logger.json(GsonConverter.setLog(response.body()));
                    if (response.body() != null && response.isSuccessful()) {
                        for (DTO_Chat item : response.body()) {
                            String now = null;
                            try {
                                now = DateConverter.setDate();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            /* 서버에 보낼 Json 만들기 */
                            DTO_Chat dto =
                                    new DTO_Chat(user_profile, room_id, user_id, user_name,
                                            "send", "이미지가 전송되었습니다.", item.getMsg(), now,
                                            "img", "" + online, "", "", home_id);
                            chat_adapter.addItem(dto);
                            String msg = DtoToJson(dto);

                            /* Send Thread 로 서버로 보내기 */
                            Send send = new Send(socket, msg);
                            send.start();
                            /* 리사이클러뷰 맨 아래로 이동 */
                            bind.chatRv.scrollToPosition(chat_adapter.getItemCount() - 1);


                            bind.chatAdd.setVisibility(View.GONE);
                            bind.chatAddBtn.setImageResource(R.drawable.plus);
                            add = false;
                        }
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<DTO_Chat>> call, Throwable t) {

                }
            });
        }
        ;
    }

    private void getScroll() {
        if (page == 1) {
            page = 2;
        }

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Chat> call = retrofitInterface.getChat(home_id, read_id, user_id, room_id, size, page);
        call.enqueue(new Callback<DTO_Chat>() {
            @Override
            public void onResponse(Call<DTO_Chat> call, Response<DTO_Chat> response) {
                if (response.body() != null && response.isSuccessful()) {
                    if (response.body().getMessages().size() > 0) {
                        page++;
                    }
                    for (DTO_Chat item : response.body().getMessages()) {
                        chat_adapter.sendItem(item);
                    }
                    scroll = false;
                }
            }

            @Override
            public void onFailure(Call<DTO_Chat> call, Throwable t) {

            }
        });

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

            // 전송된 문자중에 내가 보낸가 아닌 것만
            if (code.equals("send") && !user_id.equals(id) && room_id.equals(room)) {
                chat_adapter.addItem(message);
            }

            if (code.equals("in") || code.equals("out")) {
                online = Integer.parseInt(content);
                page = 1;
                getData(true);
            }

            if (code.equals("quit")){
                bind.chatSend.setVisibility(View.GONE);
                bind.chatEt.setEnabled(false);
                bind.chatEt.setText("상대방과 대화가 불가능합니다.");
                return;
            }

            bind.chatRv.scrollToPosition(chat_adapter.getItemCount() - 1);
        }
    };

    /* DTO => JsonString 으로 어디든 보내기 쉽게 */
    private static String DtoToJson(DTO_Chat item) {
        Gson gson = new Gson();
        String jsonStr = gson.toJson(item);
        System.out.println("ChatRoom_ // 변환된 데이터 : " + jsonStr);
        return jsonStr;
    }


    private void doMenu() {
        Dialog dialog = new Dialog(Home_Chat.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_menu);

        // 창 띄우기
        dialog.show();

        // ViewBinding
        TextView btn1 = dialog.findViewById(R.id.dialog_btn);
        TextView btn2 = dialog.findViewById(R.id.dialog_btn2);
        TextView btn3 = dialog.findViewById(R.id.dialog_btn3);

        btn1.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);
        btn3.setText("채팅방 나가기");

        // 게시글 삭제
        btn3.setOnClickListener(v -> {
            dialog.dismiss();
            HomeDelete();
        });
    }

    private void HomeDelete() {
        Dialog dialog = new Dialog(Home_Chat.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_check);

        // 창 띄우기
        dialog.show();

        // ViewBinding
        TextView content = dialog.findViewById(R.id.dialog_content_check);
        TextView yes = dialog.findViewById(R.id.dialog_btn_yes);
        TextView no = dialog.findViewById(R.id.dialog_btn_no);

        content.setText("채팅방을 나가면 채팅 목록 및 대화 내용이\n삭제되고 복구할 수 없어요. \n채팅방에서 나가시겠어요?");

        // 게시글 삭제
        yes.setOnClickListener(v -> {
            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<DTO_Chat> call = retrofitInterface.deleteChatRoom(user_id,home_id,room_id);
            call.enqueue(new Callback<DTO_Chat>() {
                @Override
                public void onResponse(Call<DTO_Chat> call, Response<DTO_Chat> response) {
                    if(response.isSuccessful() && response.body()!= null){
                        Logger.d("채팅방 없애기 문자보냄");
                        String msg = JsonMaker.makeJson(new DTO_Chat("", room_id, user_id, "", "quit", "", user_profile, "", "", "" + online, "", "", home_id));
                        Send send = new Send(socket, msg);
                        send.start();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<DTO_Chat> call, Throwable t) {
                    Logger.d("에러 메세지 발생생");
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

    private void setAlarm() {
        /* 알람 설정하기  */
        if (!alarm) {
            Logger.d(alarm);
            bind.chatAlarm.setImageResource(R.drawable.no_alarm);
            alarm = true;

            PreferenceManager.setBoolean(Home_Chat.this, "alarm" + room_id, true);
        } else {
            Logger.d(alarm);
            bind.chatAlarm.setImageResource(R.drawable.bell);
            alarm = false;
            PreferenceManager.setBoolean(Home_Chat.this, "alarm" + room_id, false);
        }
    }

    private void doAdd() {
        if (add) {
            bind.chatAdd.setVisibility(View.GONE);
            bind.chatAddBtn.setImageResource(R.drawable.plus);
            add = false;
        } else {
            bind.chatAdd.setVisibility(View.VISIBLE);
            bind.chatAddBtn.setImageResource(R.drawable.x_btn);
            add = true;
        }
    }


    private void sendMessage() {
        String message = bind.chatEt.getText().toString();
        bind.chatEt.setText("");

        if (!message.isEmpty()) {
            String now = null;
            try {
                now = DateConverter.setDate();
            } catch (ParseException e) {
                e.printStackTrace();
            }


            String msg = JsonMaker.makeJson(new DTO_Chat(user_profile, room_id, user_id, user_name, "send", message, user_profile, now, "msg", "" + online, "", "", home_id));
            chat_adapter.addItem(new DTO_Chat(user_profile, room_id, user_id, user_name, "send", message, user_profile, now, "msg", "" + online, "", "", home_id));

            Send send = new Send(socket, msg);
            send.start();

            bind.chatRv.scrollToPosition(chat_adapter.getItemCount() - 1);
        }
    }

    private void getData(boolean clear) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Chat> call = retrofitInterface.getChat(home_id, read_id, user_id, room_id, size, page);
        call.enqueue(new Callback<DTO_Chat>() {
            @Override
            public void onResponse(Call<DTO_Chat> call, Response<DTO_Chat> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 상대방 이름 및 매물정보 세팅
                    Glide.with(context).load(response.body().getHome_image()).into(bind.chatHomeItemImage);
                    bind.chatTitle.setText(response.body().getWriter_name());
                    bind.chatHomeItemTitle.setText(response.body().getHome_title());
                    DecimalFormat decimalFormat = new DecimalFormat("###,###");
                    String number = decimalFormat.format(Integer.parseInt(response.body().getHome_price()));
                    bind.chatHomeItemPrice.setText(number + " 원");
                    user_profile = response.body().getMsg_profile();
                    user_name = response.body().getMsg_name();
                    alarm = PreferenceManager.getBoolean(context, "alarm" + room_id); // default = false;

                    if (!alarm) {
                        bind.chatAlarm.setImageResource(R.drawable.bell);
                        PreferenceManager.setBoolean(Home_Chat.this, "alarm" + room_id, false);
                    } else {
                        bind.chatAlarm.setImageResource(R.drawable.no_alarm);
                        PreferenceManager.setBoolean(Home_Chat.this, "alarm" + room_id, true);
                    }

                    PreferenceManager.setString(context, "nowRoom", room_id);

                    // 채팅 목록 받아오기
                    if (clear) {
                        chat_adapter.itemsClear();
                        Logger.d("채팅목록 초기화");
                    }

                    // 채팅 목록 집어넣기
                    GsonConverter.setLog(response.body().getMessages());
                    if (response.body().getMessages() != null) {
                        for (DTO_Chat item : response.body().getMessages()) {
                            chat_adapter.sendItem(item);
                        }
                    }

                    // 채팅방 처음 들어왔을때
                    if (!in) {
                        in = true;
                        Logger.d("send , in");
                        String msg = JsonMaker.makeJson(new DTO_Chat("", room_id, user_id, "", "in", "", "", "", "", "", "", "", home_id));
                        Send send = new Send(socket, msg);
                        send.start();
                    }

                    bind.chatRv.scrollToPosition(chat_adapter.getItemCount() - 1);
                }
            }

            @Override
            public void onFailure(Call<DTO_Chat> call, Throwable t) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!delete) {
            String msg = JsonMaker.makeJson(new DTO_Chat("", room_id, user_id, user_name, "out", "", "", "", "", "", "", "", home_id));
            Send send = new Send(socket, msg);
            send.start();
        }
        PreferenceManager.removeKey(context, "nowRoom");

        EventBus.getDefault().unregister(this);

        socket = null;
        /* 등록 해제 잊지말고 하기 안하면 중복 에러 발생 */
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver);
    }

    private void keyboardDown() {
        // 키보드 내리기
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}