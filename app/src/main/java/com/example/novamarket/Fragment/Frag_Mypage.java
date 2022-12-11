package com.example.novamarket.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.novamarket.Activity.ChangeInfo;
import com.example.novamarket.Activity.MainActivity;
import com.example.novamarket.Class.GlobalAuthHelper;
import com.example.novamarket.Class.PreferenceManager;
import com.example.novamarket.EventBus.SendEvent;
import com.example.novamarket.Retrofit.DTO_user;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.FragmentMypageBinding;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Frag_Mypage extends Fragment {
    private FragmentMypageBinding bind;
    private String user_id;
    private int LoginType;
    private final int TYPE_KAKAO = 0;
    private final int TYPE_NAVER = 1;
    private final int TYPE_GOOGLE = 2;
    private final int TYPE_LOGIN = 3;

    public static Frag_Mypage newInstance() {
        Frag_Mypage fragment = new Frag_Mypage();
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
    public void onStart() {
        super.onStart();
        try {
            EventBus.getDefault().register(this);
        }catch (Exception e) {
            Logger.d("이벤트 버스 에러 : " + e.toString() );
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            EventBus.getDefault().unregister(this);
        }catch (Exception e) {
            Logger.d("이벤트 버스 에러 : " + e.toString() );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bind = FragmentMypageBinding.inflate(inflater, container, false);
        View view = bind.getRoot();
        user_id = PreferenceManager.getString(requireContext(),"user_id");

        String name = PreferenceManager.getString(requireContext(),"user_name");
        bind.myPageNick.setText(name);
        Glide.with(requireContext()).load(PreferenceManager.getString(requireContext(),"user_profile")).into(bind.myPageProfile);

        bind.myPageUserInfo.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChangeInfo.class);
            intent.putExtra("user_name", bind.myPageNick.getText().toString());
            intent.putExtra("user_profile", PreferenceManager.getString(requireContext(),"user_profile"));
            startActivity(intent);
        });

        //  판매목록
        bind.myPageSellList.setOnClickListener(v -> {
            /*
            * EvnetBus 사용해서 데이터 전달
            * DAO -> Activity 로 데이터 전송
            * */
            EventBus.getDefault().post(new SendEvent("테스트"));
            Logger.d("이벤트 버스 실행");
        });

        // 로그아웃
        bind.myPageLogout.setOnClickListener(v -> {
            LoginType = PreferenceManager.getInt(requireContext(), "loginType");
//            Logger.d("로그아웃 : " + LoginType);
            switch (LoginType) {
                case TYPE_KAKAO:
                    GlobalAuthHelper.KakaoAccountLogout(requireContext(), Frag_Mypage.this);
                    PreferenceManager.clear(requireContext());
                    break;
                case TYPE_NAVER:
                    GlobalAuthHelper.NaverAccountLogout(requireContext(), Frag_Mypage.this);
                    PreferenceManager.clear(requireContext());
                    break;
                case TYPE_GOOGLE:
                    break;
                case TYPE_LOGIN:
                    Intent intent = new Intent(requireContext(),MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                    PreferenceManager.clear(requireContext());
                    break;
                default:
                    directToMainActivity(true);
                    Logger.e("로그아웃 오류");
            }
        });

        // 연동해제(회원 탈퇴)
        bind.myPageUnlink.setOnClickListener(v -> {
            LoginType = PreferenceManager.getInt(requireContext(), "loginType");
            Logger.d("로그아웃 : " + LoginType);
            switch (LoginType) {
                case TYPE_KAKAO:
                    GlobalAuthHelper.KakaoAccountResign(requireContext(), Frag_Mypage.this);
                    deleteUser();
                    PreferenceManager.clear(requireContext());
                    break;
                case TYPE_NAVER:
                    GlobalAuthHelper.NaverAccountResign(requireContext(), Frag_Mypage.this);
                    deleteUser();
                    PreferenceManager.clear(requireContext());
                    break;
                case TYPE_GOOGLE:
                    deleteUser();
                    PreferenceManager.clear(requireContext());
                    break;
                case TYPE_LOGIN:
                    Intent intent = new Intent(requireContext(),MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                    deleteUser();
                    PreferenceManager.clear(requireContext());
                    break;
                default:
                    directToMainActivity(true);
                    Logger.e("회원탈퇴 오류");
            }
        });

        return view;
    }

    private void deleteUser() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.deleteUser(user_id);
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {

            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                    Logger.e("에러 코드 : " + t);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void getData() {
        RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
        Call<DTO_user> call = retrofitInterface.getUserInfo(user_id);
        call.enqueue(new Callback<DTO_user>() {
            @Override
            public void onResponse(Call<DTO_user> call, Response<DTO_user> response) {
                Gson gson = new Gson();
                String str = gson.toJson(response.body());
                Logger.json(str);
                if (response.isSuccessful() && response.body() != null) {
                    bind.myPageNick.setText(response.body().getUser_name());
                    Glide.with(requireContext()).load(response.body().getUser_profile()).into(bind.myPageProfile);
                    PreferenceManager.setString(requireContext(),"user_profile", response.body().getUser_profile());
                    PreferenceManager.setString(requireContext(),"user_name", response.body().getUser_name());
                }
            }

            @Override
            public void onFailure(Call<DTO_user> call, Throwable t) {
                    Logger.d("에러코드 : " + t);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bind = null;

    }

    public void directToMainActivity(boolean result) {
        if (result) {
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
            Logger.d("성공");
        } else {
            Logger.d("GlobalAuthHelper 에러, 실패 다시 로그인해주세요");
        }
    }

}