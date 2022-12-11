package com.example.novamarket.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.novamarket.Adapter.Image_adapter;
import com.example.novamarket.Adapter.ItemTouchHelperCallBack;
import com.example.novamarket.Class.GsonConverter;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.CheckEvent;
import com.example.novamarket.EventBus.HomeEvent;
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.Retrofit.DTO_Home;
import com.example.novamarket.Retrofit.DTO_Image;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityHomeWriteBinding;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_Write extends AppCompatActivity {
    private ActivityHomeWriteBinding bind;
    private String title = null, cate = null, price = null, user_id = null, content = null, writer = null, propose = "0", user_name = null;
    private String home_id = null;
    private String temp = null;
    private ArrayList<Bitmap> items = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;
    private Image_adapter adapter;
    private Context context;
    private boolean update = false;
    private boolean check_propose = true;

    private void getDate(String home_id) {
        this.home_id = home_id;
        String user_id = PreferenceManager.getString(getBaseContext(), "user_id");
        update = true; // 수정 상태 인지 확인
        // 데이터를 받아서 상태를 표시해야함
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.getHomeRead(home_id, user_id);
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                Logger.json(GsonConverter.setLog(response.body()));
                if (response.body() != null && response.isSuccessful()) {
                    for (DTO_Image image : response.body().getImages()) {
                        Glide.with(Home_Write.this).asBitmap().load(image.getImage()).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                adapter.addItem(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    }
                    bind.writeTop.setText("게시글 수정"); // 수정글로 변경
                    bind.writeTitle.setText(response.body().getTitle()); // 제목 세팅
                    cate = response.body().getCate();
                    bind.writeCate.setText(cate);
                    price = (response.body().getPrice() + "");
                    if (!price.isEmpty()) {
                        DecimalFormat decimalFormat = new DecimalFormat("###,###");
                        String number = decimalFormat.format(Integer.parseInt(price));
                        bind.writePrice.setText(number + " 원");
                    }
                    bind.writeContent.setText(response.body().getContent());
                    update = true;
                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {
                Logger.d("에러메세지 : " + t);
            }
        });
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
    }

    // 이벤트를 받음
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void callEvent(SendEvent event) {
        int total = event.pos;
        if (adapter.getItemCount() > 0) {
            bind.writeNumber.setText(adapter.getItemCount() + "");
            bind.writeNumber.setTextColor(Color.parseColor("#FF0000"));
            Logger.d("이벤트 버스 받음");
        } else {
            bind.writeNumber.setText(adapter.getItemCount() + "");
            bind.writeNumber.setTextColor(Color.parseColor("#000000"));
            Logger.d("이벤트 버스 받음");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeWriteBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Intent intent = getIntent();
        String home_id = intent.getStringExtra("home_id");
        if (!(home_id == null)) {
            getDate(home_id);
        }

        //권한 및 초기화
        context = Home_Write.this;
        user_id = PreferenceManager.getString(context, "user_id");
        user_name = PreferenceManager.getString(context, "user_name");

        // 어뎁터 세팅
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        bind.writeRv.setLayoutManager(linearLayoutManager);
        adapter = new Image_adapter(context);
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallBack(adapter));
        itemTouchHelper.attachToRecyclerView(bind.writeRv);
        bind.writeRv.setAdapter(adapter);

        // 상품 등록
        bind.writeFinish.setOnClickListener(v -> {
            if (update) {
                updateHome(); // 수정
            } else {
                writeHome();
            }
        });

        // 카테고리
        bind.writeCate.setOnClickListener(v -> {
            CreateListDialog();
        });


        // 제품 가격
        bind.writePrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bind.writeWon.setVisibility(View.VISIBLE);
                    bind.writeWon2.setVisibility(View.VISIBLE);
                    bind.writePrice.setPadding(100, 0, 0, 0);
                    bind.writePrice.setHint("");
                    bind.writePrice.setText(price);
                    bind.writePrice.setSelection(bind.writePrice.getText().toString().length());
                } else {
                    price = bind.writePrice.getText().toString();
                    bind.writeWon.setVisibility(View.INVISIBLE);
                    bind.writeWon2.setVisibility(View.INVISIBLE);
                    bind.writePrice.setHint("￦ 가격");
                    bind.writePrice.setPadding(20, 0, 0, 0);
                    if (!price.isEmpty()) {
                        DecimalFormat decimalFormat = new DecimalFormat("###,###");
                        String number = decimalFormat.format(Integer.parseInt(price));
                        bind.writePrice.setText(number + " 원");
                        bind.writePrice.setSelection(bind.writePrice.getText().toString().length());
                    }
                }
            }
        });

        // 제목
        bind.writeTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bind.writeTitle.setHint("");
                } else {
                    bind.writeTitle.setHint("제목(15자)");
                }
            }
        });

        // 가격 제안
        bind.writeProposeBtn.setOnClickListener(v -> {
            if (check_propose) {
                bind.writePropose.setChecked(false);
                check_propose = false;
                propose = "1";
            } else {
                bind.writePropose.setChecked(true);
                check_propose = true;
                price = "0";
            }
        });

        // 내용
        bind.writeContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    bind.writeContent.setHint("");
                } else {
                    bind.writeContent.setHint("게시글 내용을 작성해주세요.\\n가품 및 판매금지품목은 게시가 제한될 수 있습니다.");
                }
            }
        });

        // 이미지 가져오기
        bind.writeImg.setOnClickListener(v -> {
            getImages();
        });
    }

    private void getImages(){
        int total = 3 - adapter.getItemCount();
        if (total > 0) {
            // 내 커스텀
            Matisse.from(Home_Write.this)
                    .choose(MimeType.ofAll())
                    .countable(false)
                    .maxSelectable(total)
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .thumbnailScale(0.85f)
                    .imageEngine(new GlideEngine())
                    .showPreview(false)
                    .forResult(1111);
        } else {
            Toast.makeText(context, "이미지는 최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeHome() {
        // RequestBody 정리
        RequestBody body_user_id = RequestBody.create(MediaType.parse("text/plain"), user_id);
        RequestBody body_user_name = RequestBody.create(MediaType.parse("text/plain"), user_name);
        RequestBody body_title = RequestBody.create(MediaType.parse("text/plain"), bind.writeTitle.getText().toString());
        RequestBody body_cate = RequestBody.create(MediaType.parse("text/plain"), bind.writeCate.getText().toString());
        RequestBody body_price = RequestBody.create(MediaType.parse("text/plain"), price);
        RequestBody body_propose = RequestBody.create(MediaType.parse("text/plain"), propose);
        RequestBody body_content = RequestBody.create(MediaType.parse("text/plain"), bind.writeContent.getText().toString());

        // 비트맵 을 file 로 바꿔서 multipartBody.Part 에 저장
        items = adapter.getItems();
        ArrayList<MultipartBody.Part> files = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                File file = bitmapToFile(items.get(i), "bitmaps" + i);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-date"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", requestFile);
                files.add(body);
            }
        }

        // 일반적인 등록
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.writeHome(body_user_id, body_title, body_user_name, body_cate, body_price, body_propose, body_content, files);
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                Gson gson = new Gson();
                String str = gson.toJson(response.body());
                Logger.json(str);
                if (response.isSuccessful() && response.body() != null) {
                    String Home_id = response.body().getHome_id();
                    Intent intent = new Intent(context, Home_Read.class);
                    intent.putExtra("home_id", Home_id);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });
    }

    private void updateHome() {
        // RequestBody 정리
        RequestBody body_home_id = RequestBody.create(home_id,MediaType.parse("text/plain"));
        RequestBody body_user_id = RequestBody.create(MediaType.parse("text/plain"), user_id);
        RequestBody body_user_name = RequestBody.create(MediaType.parse("text/plain"), user_name);
        RequestBody body_title = RequestBody.create(MediaType.parse("text/plain"), bind.writeTitle.getText().toString());
        RequestBody body_cate = RequestBody.create(MediaType.parse("text/plain"), bind.writeCate.getText().toString());
        RequestBody body_price = RequestBody.create(MediaType.parse("text/plain"), price);
        RequestBody body_propose = RequestBody.create(MediaType.parse("text/plain"), propose);
        RequestBody body_content = RequestBody.create(MediaType.parse("text/plain"), bind.writeContent.getText().toString());

        // 비트맵 을 file 로 바꿔서 multipartBody.Part 에 저장
        items = adapter.getItems();
        ArrayList<MultipartBody.Part> files = new ArrayList<>();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                File file = bitmapToFile(items.get(i), "bitmaps" + i);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-date"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + i, i + "", requestFile);
                files.add(body);
            }
        }

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Home> call = retrofitInterface.updateHome(body_home_id,body_user_id,body_title,body_user_name,body_cate,body_price,body_propose,body_content,files);
        call.enqueue(new Callback<DTO_Home>() {
            @Override
            public void onResponse(Call<DTO_Home> call, Response<DTO_Home> response) {
                Logger.d("업데이트 성공");
                EventBus.getDefault().post(new CheckEvent(true));
                finish();
            }

            @Override
            public void onFailure(Call<DTO_Home> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1111 && resultCode == RESULT_OK) {
            List<Uri> image_uri = new ArrayList<>();
            image_uri = Matisse.obtainResult(data);

            for (Uri image : image_uri) {
                Glide.with(Home_Write.this)
                        .asBitmap()
                        .load(image)
                        .centerCrop()
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                /* 서버에 올릴 파일 저장 */
                                items.add(resource);
                                adapter.addItem(resource);
                                if (adapter.getItemCount() > 0) {
                                    bind.writeNumber.setText(adapter.getItemCount() + "");
                                    bind.writeNumber.setTextColor(Color.parseColor("#FF0000"));
                                } else {
                                    bind.writeNumber.setText(adapter.getItemCount() + "");
                                    bind.writeNumber.setTextColor(Color.parseColor("#000000"));
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
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


    // 한페이지에서 만들기
    public void CreateListDialog() {
        final ArrayList<String> ListItems = new ArrayList<>();
//        ListItems.add("전체");
        ListItems.add("디지털기기");
        ListItems.add("가구/인테리어");
        ListItems.add("생활/주방");
        ListItems.add("패션/잡화");
        ListItems.add("도서");
        ListItems.add("식물");
        ListItems.add("취미/게임/음반");

        final String[] items = ListItems.toArray(new String[ListItems.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("카테고리 선택");
        builder.setItems(items, (dialog, pos) -> {
            String selectedText = items[pos];
            bind.writeCate.setText(items[pos]);
            cate = items[pos];
        });
        builder.show();
    }

    // 비트맵 -> File로 변환
    private File bitmapToFile(Bitmap getBitmap, String filename) {
        // bitmap 을 담을 File 을 생성한다
        File f = new File(getCacheDir(), filename);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // bitmap 을 byte array 바꾼다
        Bitmap bitmap = getBitmap;
        // bitmap 을 byte 배열로 담는다.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // 비트맵을 png 형식으로 byte[] 으로 변환하여 bos 에 담는다.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        // byte [] 을 File 에 담는다.
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
}