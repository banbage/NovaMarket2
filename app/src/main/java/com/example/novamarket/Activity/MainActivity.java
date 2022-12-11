package com.example.novamarket.Activity;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.novamarket.Class.GoogleLogin;
import com.example.novamarket.Class.KakaoLogin;
import com.example.novamarket.Class.LoggerManager;
import com.example.novamarket.Class.NaverLogin;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_user;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.auth.Session;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthLoginState;
import com.orhanobut.logger.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding bind;
    private Context context = this;
    private KakaoLogin.KakaoSessionCallback sessionCallback;
    private OAuthLogin mNaverLoginModule;
    private FirebaseAuth mGoogleLoginModule;
    private final int TYPE_KAKAO = 0;
    private final int TYPE_NAVER = 1;
    private final int TYPE_GOOGLE = 2;
    private final int TYPE_LOGIN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        LoggerManager.setAdapter();


        // 일반 로그인
        bind.mainLoginBtn.setOnClickListener(v -> {
            String Id = bind.mainId.getText().toString();
            String pw = bind.mainPassword.getText().toString();

            PreferenceManager.setString(context,"user_id", Id);

            if (Id.isEmpty()) {
                Toast.makeText(getApplicationContext(), "아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.mainId.requestFocus();
                return;
            }

            if (pw.isEmpty()) {
                Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.mainPassword.requestFocus();
                return;
            }
            selectUser(Id, pw);
        });

        // 카카오 로그인
        bind.mainKakao.setOnClickListener(v -> {
            bind.mainKakaoBtn.performClick();

        });

        // 네이버 로그인
        // 네이버 로그인 핸들러 설정
        bind.mainNaver.setOAuthLoginHandler(mNaverLoginHandler);
        bind.mainNaver.setOnClickListener(v -> {
            Logger.d("네이버 로그인");
            mNaverLoginModule.startOauthLoginActivity(MainActivity.this, mNaverLoginHandler);
        });

        // 구글 로그인
        bind.mainGoogle.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoogleLogin.class);
            startActivity(intent);
            finish();
        });

        mNaverLoginModule = OAuthLogin.getInstance();
        mNaverLoginModule.init(
                getBaseContext(),
                getString(R.string.naver_client_id),
                getString(R.string.naver_client_secret),
                getString(R.string.naver_client_name)
        );

        // 회원가입
        bind.mainJoin.setOnClickListener(v -> {
            Intent intent = new Intent(this, Join.class);
            startActivity(intent);
        });

        // 아이디 찾기
        bind.mainFindId.setOnClickListener(v -> {

        });
        // 비밀번호 찾기
        bind.mainFindPassword.setOnClickListener(v -> {

        });

        // 자동 로그인
        // 카카오, 네이버 로그인 세션이 둘다 없다면
        // 네이버 : 핸들러가 세션 없을 시 동작을 대체한다.
        FirebaseApp.initializeApp(context);

        mGoogleLoginModule = FirebaseAuth.getInstance();
        if (!HasKakaoSession() && !HasNaverSession() && !HasGoogleSession()) {
            sessionCallback = new KakaoLogin.KakaoSessionCallback(getApplicationContext(), MainActivity.this);
            Session.getCurrentSession().addCallback(sessionCallback);
            Logger.d("onClick : 로그인 세션 없음");
            // 카카오 로그인 세션
        } else if (HasKakaoSession()) {
            Logger.d("onClick : 카카오 로그인");
            sessionCallback = new KakaoLogin.KakaoSessionCallback(getApplicationContext(), MainActivity.this);
            Session.getCurrentSession().addCallback(sessionCallback);
            Session.getCurrentSession().checkAndImplicitOpen();
        } else if (HasNaverSession()) {
            Logger.d("onClick : 네이버 로그인");
            Intent intent = new Intent(MainActivity.this, NaverLogin.class);
            startActivity(intent);
            finish();
        } else if (HasGoogleSession()) {
            Intent intent = new Intent(MainActivity.this, GoogleLogin.class);
            startActivity(intent);
            finish();
        }


        // 권한 요청
        // 마시멜로우 버전 이후라면 권한을 요청해라
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                Logger.d("권한설정 성공");
            } else {
                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS};
                ActivityCompat.requestPermissions(this, permission, 1);
            }
        }

        // 일반 로그인시 자동 로그인 체크
        if (PreferenceManager.getBoolean(getBaseContext(), "auto")) {
            Intent intent = new Intent(MainActivity.this, Home.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean HasGoogleSession() {
        return mGoogleLoginModule.getCurrentUser() != null;
    }

    // 네이버 세션 확인
    private boolean HasNaverSession() {
        if (OAuthLoginState.NEED_LOGIN.equals(mNaverLoginModule.getState(getApplicationContext())) ||
                OAuthLoginState.NEED_INIT.equals(mNaverLoginModule.getState(getApplicationContext()))) {
            return false;
        }
        return true;
    }


    private void selectUser(String id, String pw) {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.getUserLogin(id, pw);
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isUser_check()) {
                        if (bind.mainAutoLogin.isChecked()) {
                            PreferenceManager.setBoolean(getBaseContext(), "auto", true);
                        } else {
                            PreferenceManager.setBoolean(getBaseContext(), "auto", false);
                        }
                        PreferenceManager.setInt(getBaseContext(), "loginType", TYPE_LOGIN);
                        PreferenceManager.setString(getBaseContext(), "user_id", response.body().getUser_id());
                        PreferenceManager.setString(getBaseContext(), "user_name", response.body().getUser_name());
                        PreferenceManager.setString(getBaseContext(), "user_profile", response.body().getUser_profile());

                        Logger.d("Profile : " + response.body().getUser_profile() + ", ID : " + response.body().getUser_id() + ", name : " + response.body().getUser_name());

                        Intent intent = new Intent(MainActivity.this, Home.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "회원정보가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*  더이상 세션 상태 변화 콜백을 받고 싶지 않을 때 삭제한다.
            Params:
            callback – 삭제할 콜백 */
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    /*
    *  Session 의 상태를 체크후 isOpenable() 상태일 때 Login을 시도한다.
        요청에 대한 결과는 KakaoAdapter의 ISessionCallback으로 전달이 된다.
        Returns:
        true if token can be refreshed, false otherwise
    *  */
    private boolean HasKakaoSession() { // 현재 세션 상태를 가져옵니다.
        if (!Session.getCurrentSession().checkAndImplicitOpen()) {
            return false; // token refresh 불가능 => 즉, 다시 연결해야댐
        }
        return true; // refresh 토큰 있음 => 즉, 바로 연결 간으
    }

    public void directToSecondActivity(boolean result) {
        Intent intent = new Intent(MainActivity.this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (result) {
            Toast.makeText(getApplicationContext(), "로그인 성공!", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
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

    @SuppressLint("HandlerLeak")
    private OAuthLoginHandler mNaverLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                PreferenceManager.setInt(getBaseContext(), "loginType", TYPE_NAVER);
                Intent intent = new Intent(MainActivity.this, NaverLogin.class);
                startActivity(intent);
                finish();
            } else {
                Logger.d("로그인 실패 : errorCode : " + mNaverLoginModule.getLastErrorCode(context) + " errorDesc : " + mNaverLoginModule.getLastErrorDesc(context));
            }
        }
    };
}

