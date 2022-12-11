package com.example.novamarket.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_user;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityChangeInfoBinding;
import com.example.novamarket.databinding.ActivityUserInfoBinding;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeInfo extends AppCompatActivity {
    ActivityChangeInfoBinding bind;
    private String name, profile, id;
    private Bitmap bitmap;
    private InputMethodManager imm;
    // 오픈
    private final int ROOM_TYPE_OPEN = 0;
    private final int ROOM_TYPE_CLOSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityChangeInfoBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        // 초기화
        Intent intent = getIntent();
        id = PreferenceManager.getString(getBaseContext(), "user_id");
        name = intent.getStringExtra("user_name");
        profile = intent.getStringExtra("user_profile");
        bind.changeName.setText(name);
        Glide.with(getBaseContext()).asBitmap().load(profile).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                bitmap = resource;
                bind.changeImage.setImageBitmap(bitmap);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

        bind.changeBtn.setOnClickListener(v -> {
            name = bind.changeName.getText().toString();

            File file = bitmapToFile(bitmap, "file");
            RequestBody reName = RequestBody.create(name, MediaType.parse("text/plain"));
            RequestBody reId = RequestBody.create(id, MediaType.parse("text/plain"));
            RequestBody requestFile = RequestBody.create(file, MediaType.parse("multipart/form-date"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("profile", "profile", requestFile);

            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<DTO_user> call = retrofitInterface.updateUser(reId, reName , body);
            call.enqueue(new Callback<DTO_user>() {
                @Override
                public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                    Gson gson = new Gson();
                    String str = gson.toJson(response.body());
                    Logger.json(str);
                    if(response.body() != null && response.isSuccessful()){
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<DTO_user> call, Throwable t) {
                    Logger.d("에러코드 : " + t);
                }
            });
        });

        bind.changeImage.setOnClickListener(v -> {
            Matisse.from(ChangeInfo.this)
                    .choose(MimeType.ofAll())
                    .countable(false)
                    .maxSelectable(1)
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .thumbnailScale(0.85f)
                    .imageEngine(new GlideEngine())
                    .showPreview(false)
                    .forResult(1111);
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1111 && resultCode == RESULT_OK) {
            Uri image_uri = Matisse.obtainResult(data).get(0);
            Glide.with(ChangeInfo.this)
                    .asBitmap()
                    .load(image_uri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            /* 서버에 올릴 파일 저장 */
                            bind.changeImage.setImageBitmap(resource);
                            bitmap = resource;
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        }
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

    @Override
    /* 키보드 자동 내리기 세팅 */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            // 사각형을 만드는 클래스 (Rectangle) 직사각형
            Rect rect = new Rect();
            // focus 된 View 의 전체 면적을 가져옴
            focusView.getGlobalVisibleRect(rect);
            // 현재 이벤트가 일어난 x, y 좌표를 가져옴
            int x = (int) ev.getX(), y = (int) ev.getY();
            // 클릭이벤트가 focusView 범위 안에 일어났는지 확인
            if (!rect.contains(x, y)) {
                imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}