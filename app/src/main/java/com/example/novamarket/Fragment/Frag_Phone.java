package com.example.novamarket.Fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.novamarket.Activity.UserInfo;
import com.example.novamarket.Class.GenerateCertNumber;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.Class.SMSReceiver;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_user;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.FragmentFragPhoneBinding;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 */
public class Frag_Phone extends Fragment {
    private FragmentFragPhoneBinding bind;
    private InputMethodManager imm;
    private CountDownTimer countDownTimer; // 타이머 객체
    private static final long START_TIME_MILLTS = 10000; // 총시간
    private long mTimeLeftInMillis = START_TIME_MILLTS; // 남은 시간
    private boolean mTimerRunning = false; // 타이머 작동체크
    private boolean Auth = false;
    private boolean changeActivity = false;
    private String phone_number, Auth_number;
    private SMSReceiver smsReceiver;

    public static Frag_Phone newInstance() {
        Frag_Phone fragment = new Frag_Phone();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bind = FragmentFragPhoneBinding.inflate(inflater, container, false);
        View view = bind.getRoot();

        imm = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);

        // 인증번호 전송
        bind.phoneSubmit.setOnClickListener(v -> {
            // 1. 핸드폰 번호 중복 확인
            // 2. 중복이 아닐시에 => 인증번호 및 번호 저장
            // 3. 인증번호 문자 발송 => 카운트다운 시작, 다시보내기 On, 휴대폰번호 수정불가 및 버튼 클릭불가
            // 4. 인증번호 확인 => 인증번호 똑같을시 Auth = ture;
            // 5. 다음버튼 => cert = true 일댸만 발송 => false 로 전환

            // 유효성 체크
            if (bind.phoneNumber.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "휴대폰 번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.phoneNumber.requestFocus();
                return;
            }

            if (bind.phoneNumber.getText().toString().length() < 11) {
                Toast.makeText(requireContext(), "휴대폰 번호를 정확히 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.phoneNumber.requestFocus();
                return;
            }
            // 타이머 작동중이 아니라면 타이머 시작
            if (!mTimerRunning) {
                sendSMS();
                Logger.d("타이머 시작");
            }
            HideKeyboard();
        });

        // 인증번호 체크
        bind.phoneCheck.setOnClickListener(v -> {
            if (Auth_number.equals(bind.phoneAuth.getText().toString())) {
                // 버튼 이용 불가 설정
                bind.phoneNumber.setEnabled(false);
                bind.phoneReset.setClickable(false);
                bind.phoneReset.setFocusable(false);
                bind.phoneReset.setTextColor(Color.parseColor("#000000"));
                bind.phoneCheck.setEnabled(false);
                bind.phoneReset.setText("인증이 완료 되었습니다.");
                stopTimer();
                // 인증 확인
                Auth = true;
            } else {
                Toast.makeText(requireContext(), "인증번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                Logger.d("인증번호 다름 Auth_number : " + Auth_number + " , phoneAuth : " + bind.phoneAuth.getText().toString() + "  ");
            }

            HideKeyboard();
        });

        // 초기화 버튼
        bind.phoneReset.setOnClickListener(v -> {
            Auth_number = "";
            sendSMS();
            resetTimer();
            HideKeyboard();
        });

        // 회원정보 입력창으로 이동
        bind.phoneNext.setOnClickListener(v -> {
            if (Auth) {
                Intent intent = new Intent(requireContext(), UserInfo.class);
                intent.putExtra("user_id", phone_number);
                startActivity(intent);
                requireActivity().finish();
            } else {
                Toast.makeText(requireContext(), "본인인증을 진행해주세요", Toast.LENGTH_SHORT).show();
                bind.phoneNumber.requestFocus();
            }

            HideKeyboard();
        });
        return view;
    }

    private void sendSMS() {
        phone_number = bind.phoneNumber.getText().toString();
        PreferenceManager.setInt(requireContext(), "sns_receive", 0);

        // 전송할 전화번호
        String phoneNumStr = "5554";
        // 인증 번호 생성
        GenerateCertNumber generateCertNumber = new GenerateCertNumber();
        String SMSContent = generateCertNumber.excuteGenerate();
        Auth_number = SMSContent;

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.getUserId(bind.phoneNumber.getText().toString());
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isUser_check()) {

                        Gson gson = new Gson();
                        String str = gson.toJson(response.body());
                        Logger.json(str);
                        // 중복없음
                        // 문자 전송
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNumStr, null, "[NovaMarket] 인증번호는 [ " + SMSContent + " ] 입니다.", null, null);

                            //리셋 버튼 보이기, 카운트 다운 쓰레드 시작
                            bind.phoneReset.setVisibility(View.VISIBLE);
                            bind.phoneTimer.setVisibility(View.VISIBLE);
                            bind.phoneSubmit.setEnabled(false);
                            bind.phoneNumber.setEnabled(false);
                            bind.phoneAuth.setEnabled(true);
                            bind.phoneCheck.setEnabled(true);
                            startTimer();

                        } catch (Exception e) {
                            Logger.d("에러 메세지" + e);
                            e.printStackTrace();
                        }
                    } else {
                        // 중복있음
                        Toast.makeText(requireContext(), "이미 가입된 전화번호 입니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });

    }

    private void startTimer() {
        // 카운트 다운 시간 설정( 총 진행시간, 1초씩 감소)
        countDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateCountDownText();
                // 1초가 지날떄마다 시간 업로드
                mTimeLeftInMillis = millisUntilFinished; //남은시간 등록
                mTimerRunning = true;
            }

            @Override
            public void onFinish() {
                if (!changeActivity) {
                    // 타이머가 종료되면 실행 할 일
                    bind.phoneNumber.setEnabled(true);
                    bind.phoneSubmit.setEnabled(true);
                    mTimerRunning = false;
                    bind.phoneReset.setVisibility(View.INVISIBLE);
                    bind.phoneTimer.setVisibility(View.INVISIBLE);
                    bind.phoneCheck.setEnabled(false);
                    bind.phoneAuth.setEnabled(false);
                    bind.phoneAuth.setText("");
                    bind.phoneNumber.requestFocus();
                    mTimeLeftInMillis = START_TIME_MILLTS;
                    Auth_number = "";
                    // 종료직전에 화면 전환하면 생기는 오류 방지

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("입력 시간 초과");
                    builder.setMessage("입력시간이 초과하였습니다.\n번호 인증을 다시 요청해 주십시요");
                    builder.setPositiveButton("확인", null);
                    builder.create().show();
                }
            }
        }.start();
        mTimerRunning = true;
    }

    private void resetTimer() {
        countDownTimer.cancel();
        mTimeLeftInMillis = START_TIME_MILLTS;
        updateCountDownText();
        startTimer();
    }

    private void stopTimer() {
        countDownTimer.cancel();
        mTimerRunning = false;
        bind.phoneTimer.setVisibility(View.INVISIBLE);
    }

    private void updateCountDownText() {
        if (!changeActivity) {
            // 분, 초 표현
            int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
            int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

            // ??분 ??초 로 포멧
            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d분 %02d초", minutes, seconds);
            bind.phoneTimer.setText(timeLeftFormatted);
        }
    }

    private void HideKeyboard() {
        if (requireActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void setAuthNum(String sns_number) {
        bind.phoneAuth.setText(sns_number);
    }

    @Override
    public void onPause() {
        super.onPause();
        mTimerRunning = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            changeActivity = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bind = null;
    }
}