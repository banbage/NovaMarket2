package com.example.novamarket.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_user;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityUserInfoBinding;
import com.orhanobut.logger.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserInfo extends AppCompatActivity {
    ActivityUserInfoBinding bind;
    private String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityUserInfoBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        user_id = getIntent().getStringExtra("user_id");
        bind.userInfoId.setText(user_id);


        bind.userInfoJoin.setOnClickListener(v -> {
            String user_id = bind.userInfoId.getText().toString();
            String user_name = bind.userInfoName.getText().toString();
            String password = bind.userInfoPassword.getText().toString();
            String check = bind.userInfoCheck.getText().toString();

            if(user_id.isEmpty()){
                Toast.makeText(getApplicationContext(), "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.userInfoId.requestFocus();
                return;
            }
            if(user_name.isEmpty()){
                Toast.makeText(getApplicationContext(),"닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.userInfoName.requestFocus();
                return;
            }
            if(password.isEmpty()){
                Toast.makeText(getApplicationContext(),"비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.userInfoPassword.requestFocus();
                return;
            }
            if(!password.equals(check)){
                Toast.makeText(getApplicationContext(),"비밀번호가 일치하지않습니다.",Toast.LENGTH_SHORT).show();
                bind.userInfoPassword.setText("");
                bind.userInfoCheck.setText("");
                bind.userInfoPassword.requestFocus();
            }

            userJoin(user_id,user_name,password);
        });

    }

    private void userJoin(String user_id, String user_name, String password) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.writeUser(user_id,user_name,password);
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                Logger.d("에러 코드 : " + t);
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
}