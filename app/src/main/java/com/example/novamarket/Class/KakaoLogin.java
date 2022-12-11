package com.example.novamarket.Class;

import android.app.Activity;
import android.content.Context;

import com.example.novamarket.Activity.MainActivity;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.exception.KakaoException;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class KakaoLogin extends Activity {
    /*
    * 1. 카카오 로그인
    * 2. 회원정보 요청
    * */

    public static class KakaoSessionCallback implements ISessionCallback {
        private Context mContext;
        private MainActivity mainActivity;
        private final int TYPE_KAKAO = 0;


        public KakaoSessionCallback(Context context, MainActivity activity) {
            this.mContext = context;
            this.mainActivity = activity;
        }

        @Override
        public void onSessionOpened() {
            requestMe();
            PreferenceManager.setInt(mContext, "loginType", TYPE_KAKAO);
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Logger.d("KaKao 로그인 오류가 발생했습니다. " + e.toString());
        }

        protected void requestMe() { // 회원 정보 요청하기
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    mainActivity.directToSecondActivity(false);
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    List<String> userInfo = new ArrayList<>();
                    userInfo.add(result.getKakaoAccount().getProfile().getProfileImageUrl()); // 유저 프로필
                    userInfo.add(result.getKakaoAccount().getProfile().getNickname()); // 유저 닉네임
                    userInfo.add(String.valueOf(result.getId())); // 유저 아이디

                    // 받아온 데이터를 GlobalHelper 에 담기
                    GlobalHelper mGlobalHelper = new GlobalHelper();
                    mGlobalHelper.setGlobalUserLoginInfo(userInfo);
                    mainActivity.directToSecondActivity(true);
                }
            });
        }
    }
}
