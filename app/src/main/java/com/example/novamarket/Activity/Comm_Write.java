package com.example.novamarket.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Comm;
import com.example.novamarket.Retrofit.DTO_Image;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityCommWriteBinding;
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
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comm_Write extends AppCompatActivity {
    private ActivityCommWriteBinding bind;
    private Image_adapter adapter;
    private ArrayList<Bitmap> items = new ArrayList<>();
    private String user_id, user_name, comm_id, cate;
    private boolean update = false, click = false;
    private Context context;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityCommWriteBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        EventBus.getDefault().register(this);

        //권한 및 초기화
        context = getBaseContext();
        user_id = PreferenceManager.getString(context, "user_id");
        user_name = PreferenceManager.getString(context, "user_name");

        // 어뎁터 세팅
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        bind.writeRv.setLayoutManager(linearLayoutManager);
        adapter = new Image_adapter(context);
        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallBack(adapter));
        itemTouchHelper.attachToRecyclerView(bind.writeRv);
        bind.writeRv.setAdapter(adapter);

        // 게시글 수정 여부 확인
        Intent intent = getIntent();
        comm_id = intent.getStringExtra("comm_id");
        if (!(comm_id == null)) {
            getData();
        }

        // 완료 버튼 클릭시 =>  수정되거나 작성됨
        // 수정시에는 수정 버튼 타고 들어왔을경우만 intent 로 home_id 를 받음
        bind.writeFinish.setOnClickListener(v -> {

            if (update && !click) {
                click = true;
                /* 게시글 수정 */
                updateComm();
                Logger.d("게시글 수정수정");
            } else {
                /* 게시글 등록 */
                click = true;
                writeComm();
            }
        });

        // 이미지
        bind.writeImg.setOnClickListener(v -> {
            getImages();
        });

        // 카테고리 선택
        bind.writeCate.setOnClickListener(v -> {
            CreateListDialog();
        });

        /*====*/
    }


    /**
     * 게시글 주제 선택
     */
    public void CreateListDialog() {
        final ArrayList<String> ListItems = new ArrayList<>();
        ListItems.add("일상");
        ListItems.add("질문");
        ListItems.add("맛집");
        ListItems.add("취미");

        final String[] items = ListItems.toArray(new String[ListItems.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게시글 주제 선택");
        builder.setItems(items, (dialog, pos) -> {
            String selectedText = items[pos];
            bind.writeCate.setText(items[pos]);
            cate = items[pos];
        });
        builder.show();
    }

    private void getImages(){
        int total = 3 - adapter.getItemCount();
        if (total > 0) {
            // 내 커스텀
            Matisse.from(Comm_Write.this)
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


    /** 게시글 수정시 데이터 가져오기*/
    private void getData() {
        update = true;
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_Comm> call = retrofitInterface.getCommRead(comm_id,user_id,1,8);
        call.enqueue(new Callback<DTO_Comm>() {
            @Override
            public void onResponse(Call<DTO_Comm> call, Response<DTO_Comm> response) {
                Logger.json(GsonConverter.setLog(response.body()));
                if(response.body() != null && response.isSuccessful()){
                    for (DTO_Image image : response.body().getImages()) {
                        Logger.d(response.body().getImages());
                        Glide.with(Comm_Write.this).asBitmap().load(image.getImage()).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                adapter.addItem(resource);;
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    }
                    bind.writeTop.setText("게시글 수정");
                    bind.writeCate.setText(response.body().getCate());
                    bind.writeContent.setText(response.body().getContent());
                }
            }

            @Override
            public void onFailure(Call<DTO_Comm> call, Throwable t) {
                Logger.d("에러메세지 : " + t);
            }
        });

    }

    /** 게시글 작성하기*/
    private void writeComm() {
        // RequestBody 정리
        RequestBody body_user_id = RequestBody.create(MediaType.parse("text/plain"), user_id);
        RequestBody body_cate = RequestBody.create(MediaType.parse("text/plain"), bind.writeCate.getText().toString());
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
        Call<DTO_Comm> call = retrofitInterface.writeComm(body_user_id,body_content,body_cate,files);
        call.enqueue(new Callback<DTO_Comm>() {
            @Override
            public void onResponse(Call<DTO_Comm> call, Response<DTO_Comm> response) {
                Logger.json(GsonConverter.setLog(response.body()));
                if(response.isSuccessful() && response.body() != null){
                  Intent intent = new Intent(context, Comm_Read.class);
                  intent.putExtra("comm_id", response.body().getComm_id());
                  startActivity(intent);
                  finish();
                }
            }

            @Override
            public void onFailure(Call<DTO_Comm> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });

    }
    /** 게시글 수정하기*/
    private void updateComm() {
        // RequestBody 정리
        RequestBody body_comm_id = RequestBody.create(comm_id, MediaType.parse("text/plain"));
        RequestBody body_user_id = RequestBody.create(MediaType.parse("text/plain"), user_id);
        RequestBody body_cate = RequestBody.create(MediaType.parse("text/plain"), bind.writeCate.getText().toString());
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
        Call<DTO_Comm> call = retrofitInterface.updateComm(body_comm_id,body_user_id,body_content,body_cate,files);
        call.enqueue(new Callback<DTO_Comm>() {
            @Override
            public void onResponse(Call<DTO_Comm> call, Response<DTO_Comm> response) {
                Logger.json(GsonConverter.setLog(response.body()));
                EventBus.getDefault().post(new CheckEvent(true));
                finish();
            }

            @Override
            public void onFailure(Call<DTO_Comm> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });

    }

    /** 이미지 가져왔을때 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1111 && resultCode == RESULT_OK) {
            List<Uri> image_uri = new ArrayList<>();
            image_uri = Matisse.obtainResult(data);

            for (Uri image : image_uri) {
                Glide.with(Comm_Write.this)
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


    /** 비트맵을 파일로 변환하기 */
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

}