package com.example.novamarket.Fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.novamarket.Activity.UserInfo;
import com.example.novamarket.Class.GenerateCertNumber;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.Class.SMSReceiver;
import com.example.novamarket.Retrofit.DTO_user;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.FragmentFragEmailBinding;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Frag_Email extends Fragment {
    private FragmentFragEmailBinding bind;
    private InputMethodManager imm;
    private CountDownTimer countDownTimer; // 타이머 객체
    private static final long START_TIME_MILLTS = 30000; // 총시간
    private long mTimeLeftInMillis = START_TIME_MILLTS; // 남은 시간
    private boolean mTimerRunning = false; // 타이머 작동체크
    private boolean Auth = false;
    private boolean changeActivity = false;
    private String phone_number, Auth_number;
    private SMSReceiver smsReceiver;


    public static Frag_Email newInstance() {
        Frag_Email fragment = new Frag_Email();
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
        bind = FragmentFragEmailBinding.inflate(inflater,container,false);
        View view = bind.getRoot();

        imm = (InputMethodManager) requireActivity().getSystemService(INPUT_METHOD_SERVICE);

        // 인증번호 전송
        bind.emailSubmit.setOnClickListener(v -> {


            // 유효성 체크
            if (bind.emailId.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                bind.emailId.requestFocus();
                return;
            }


            // 이메일 형식이 올바르지 않을 경우, 실행 취소
            Pattern pattern = Patterns.EMAIL_ADDRESS;
            if(!pattern.matcher(bind.emailId.getText().toString()).matches()){
                Toast.makeText(requireContext(), "이메일형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                bind.emailId.requestFocus();
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
        bind.emailCheck.setOnClickListener(v -> {
            if (Auth_number.equals(bind.emailAuth.getText().toString())){
                // 버튼 이용 불가 설정
                bind.emailId.setEnabled(false);
                bind.emailReset.setClickable(false);
                bind.emailReset.setFocusable(false);
                bind.emailReset.setTextColor(Color.parseColor("#000000"));
                bind.emailCheck.setEnabled(false);
                bind.emailReset.setText("인증이 완료 되었습니다.");
                stopTimer();
                // 인증 확인
                Auth = true;
            }else{
                Toast.makeText(requireContext(),"인증번호가 틀렸습니다.",Toast.LENGTH_SHORT).show();
                Logger.d("인증번호 다름 Auth_number : "+Auth_number+" , phoneAuth : "+bind.emailAuth.getText().toString()+"  ");
            }

            HideKeyboard();
        });

        // 초기화 버튼
        bind.emailReset.setOnClickListener(v -> {
            Auth_number = "";
            sendSMS();
            resetTimer();
            HideKeyboard();
        });

        // 회원정보 입력창으로 이동
        bind.emailNext.setOnClickListener(v -> {
            if (Auth) {
                Intent intent = new Intent(requireContext(), UserInfo.class);
                intent.putExtra("user_id", phone_number);
                startActivity(intent);
                requireActivity().finish();
            } else {
                Toast.makeText(requireContext(), "본인인증을 진행해주세요", Toast.LENGTH_SHORT).show();
                bind.emailId.requestFocus();
            }

            HideKeyboard();
        });



        return view;
    }

    private void sendSMS() {
        phone_number = bind.emailId.getText().toString();
        PreferenceManager.setInt(requireContext(),"sns_receive", 0);

        // 전송할 전화번호
        String phoneNumStr = "5554";
        // 인증 번호 생성
        GenerateCertNumber generateCertNumber = new GenerateCertNumber();
        String SMSContent = generateCertNumber.excuteGenerate();
        Auth_number = SMSContent;

        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.getUserId(bind.emailId.getText().toString());
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isUser_check()) {
                        Gson gson = new Gson();
                        String str = gson.toJson(response.body());
                        Logger.json(str);
                        // 중복없음
                        // 이메일 전송
                       try {
                           sendMail();

                            //리셋 버튼 보이기, 카운트 다운 쓰레드 시작
                            bind.emailReset.setVisibility(View.VISIBLE);
                            bind.emailTimer.setVisibility(View.VISIBLE);
                            bind.emailSubmit.setEnabled(false);
                            bind.emailId.setEnabled(false);
                            bind.emailAuth.setEnabled(true);
                            bind.emailCheck.setEnabled(true);
                            startTimer();

                        } catch (Exception e) {
                            Logger.d("에러 메세지" + e);
                            e.printStackTrace();
                        }
                    } else {
                        // 중복있음
                        Toast.makeText(requireContext(), "이미 가입된 이메일 입니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                Logger.d("에러 메세지 : " + t);
            }
        });
    }

    private void sendMail() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.sendMail(bind.emailId.getText().toString(), Auth_number);
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                if(response.body() != null && response.isSuccessful()){

                }
            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                Logger.d("에러코드 : " + t);
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
                bind.emailId.setEnabled(true);
                bind.emailSubmit.setEnabled(true);
                mTimerRunning = false;
                bind.emailTimer.setVisibility(View.INVISIBLE);
                bind.emailReset.setVisibility(View.INVISIBLE);
                bind.emailCheck.setEnabled(false);
                bind.emailAuth.setEnabled(false);
                bind.emailAuth.setText("");
                bind.emailId.requestFocus();
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
        bind.emailTimer.setVisibility(View.INVISIBLE);
    }

    private void updateCountDownText() {
        if (!changeActivity) {
            // 분, 초 표현
            int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
            int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

            // ??분 ??초 로 포멧
            String timeLeftFormatted = String.format(Locale.getDefault(), "%02d분 %02d초", minutes, seconds);
            bind.emailTimer.setText(timeLeftFormatted);
        }
    }

    private void HideKeyboard() {
        if (requireActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }
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