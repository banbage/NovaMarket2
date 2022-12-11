package com.example.novamarket.Class;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.novamarket.Activity.Home;
import com.example.novamarket.Fragment.Frag_Mypage;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.data.OAuthLoginState;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NaverLogin extends Activity {
    final String NAVER_RESPONSE_CODE = "00"; // 정상 반환 시 코드
    final String[] NAVER_JSON_KEY = {"id", "nickname", "profile_image"};
    private final int TYPE_NAVER = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggerManager.setAdapter();
        OAuthLogin mNaverLoginModule = OAuthLogin.getInstance();

        // 접속 토큰 갱신 및 사용자 정보 요청
        String accessToken = mNaverLoginModule.getAccessToken(getApplicationContext());
        // 엑세스 토큰이 존재하고, 현재 로그인 상태가 OK 라면
        if (accessToken != null && OAuthLoginState.OK.equals(mNaverLoginModule.getState(getApplicationContext()))) {
            // 네이버 유저 정보 가져오기
            ReqNHNUserInfo reqNaverUserInfo = new ReqNHNUserInfo();
            reqNaverUserInfo.execute(accessToken);

        } else {
            RefreshNHNToken tokenRefresh = new RefreshNHNToken();
            try {
                // 토큰이 존재하지 않는 경우
                // 엑세스 토큰 갱신, 엑세스 토큰 가져오기
                tokenRefresh.execute().get();
                Logger.e("네이버 로그인 리프레쉬");
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e("에러 코드 " + e.toString());
            }
            ReqNHNUserInfo reqNaverUserInfo = new ReqNHNUserInfo();
            reqNaverUserInfo.execute(mNaverLoginModule.getAccessToken(getApplicationContext()));
        }
        PreferenceManager.setInt(getBaseContext(), "loginType", TYPE_NAVER);
    }

    // 토큰 갱신 요청
    class RefreshNHNToken extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                OAuthLogin mNaverLoginModule = OAuthLogin.getInstance();
                mNaverLoginModule.refreshAccessToken(getApplicationContext());
                Logger.e("토큰 갱신 요청");
            } catch (Exception e) {
                Log.e("Error RefreshNHNToken", e.toString());
            }
            return true;
        }
    }

    // 사용자 정보 요청
    class ReqNHNUserInfo extends AsyncTask<String, Void, String> {
        String result;

        @Override
        protected String doInBackground(String... strings) {
            Logger.e("네이버 사용자 정보 요청");
            String token = strings[0];// 네이버 로그인 접근 토큰;
            String header = "Bearer " + token; // Bearer 다음에 공백 추가
            try {
                // 토근 ID 로 해당 URL 로 데이터 요청
                String apiURL = "https://openapi.naver.com/v1/nid/me";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", header);
                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                result = response.toString();
                br.close();
            } catch (Exception e) {
                Logger.e("네이버 유저 정보 에러 : " + e.toString());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject object = new JSONObject(result); // 가져온 네이버 정보
                Logger.d("네이버 JsonObject : " + object.toString());
                // 정삭적으로 반환을 받았을때
                if (object.getString("resultcode").equals(NAVER_RESPONSE_CODE)) {    // 정상 반환 시 반환코드는 "00"이다.
                    List<String> userInfo = new ArrayList<>();
                    JSONObject jsonObject = new JSONObject(object.getString("response"));
                    userInfo.add(jsonObject.getString(NAVER_JSON_KEY[2])); // USER_PROFILE
                    userInfo.add(jsonObject.getString(NAVER_JSON_KEY[1])); // USER_NICKNAME
                    userInfo.add(String.format("%s-%s", "NAVER", jsonObject.getString(NAVER_JSON_KEY[0]))); // USER_ID
                    GlobalHelper mGlobalHelper = new GlobalHelper();
                    mGlobalHelper.setGlobalUserLoginInfo(userInfo);
                    Intent intent = new Intent(NaverLogin.this, Home.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class DeleteTokenTask extends AsyncTask<Context, Void, Boolean> {
        Context context;
        Frag_Mypage Fragment;

        public DeleteTokenTask(Context mContext, Frag_Mypage mActivity) {
            this.context = mContext;
            this.Fragment = mActivity;
        }

        @Override
        protected Boolean doInBackground(Context... contexts) {
            // ClientID  가져오기
            return OAuthLogin.getInstance().logoutAndDeleteToken(contexts[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Fragment.directToMainActivity(result);
        }
    }
}
