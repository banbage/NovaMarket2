package com.example.novamarket.Class;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.novamarket.Activity.Home;
import com.example.novamarket.Fragment.Frag_Mypage;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.auth.ApiErrorCode;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.data.OAuthLoginState;
import com.orhanobut.logger.Logger;

public class GlobalAuthHelper {
    /*
    * 1. 로그아웃
    * 2. 회원탈퇴
    * */

    // 카카오 로그아웃
    // 로그아웃
    public static void KakaoAccountLogout(Context context, Frag_Mypage frag) {
        if (Session.getCurrentSession().checkAndImplicitOpen()) {
            UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                @Override
                public void onCompleteLogout() {
                    frag.directToMainActivity(true);
                }
            });
        }
    }

    // 카카오
    // 계정연동해제
    // => 회원탈퇴
    public static void KakaoAccountResign(final Context context, final Frag_Mypage frag) {
        if (Session.getCurrentSession().checkAndImplicitOpen()) {
            // 카카오 연동 해제
            UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    if (errorResult.getErrorCode() == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Logger.d("네트워크가 불안정합니다.");
                        frag.directToMainActivity(false);
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Logger.d("세션이 닫혀있습니다.");
                    frag.directToMainActivity(false);
                }

                @Override
                public void onSuccess(Long result) {
                    Logger.d("계정 연동 해제 완료");
                    frag.directToMainActivity(true);
                }
            });
        }
    }
    // 네이버 로그아웃
    public static void NaverAccountLogout(final Context context, final Frag_Mypage frag) {
        if(OAuthLoginState.OK.equals(OAuthLogin.getInstance().getState(context))) {
            OAuthLogin.getInstance().logout(context);
            frag.directToMainActivity(true);
        }
    }
    // 네이버 연동해제
    public static void NaverAccountResign(final Context context, final Frag_Mypage frag){
        if(OAuthLoginState.OK.equals(OAuthLogin.getInstance().getState(context))) {

            try {
                NaverLogin.DeleteTokenTask deleteTokenTask = new NaverLogin.DeleteTokenTask(context, frag);
                deleteTokenTask.execute(context).get();
                frag.directToMainActivity(true);
            }catch (Exception e){
                e.printStackTrace();
                frag.directToMainActivity(false);
                Logger.d("에러 코드, 네이버 회원탈퇴 실패 : " + e.toString());
            }
        }
    }

    // 구글 로그아웃, 앱 연동 해제
    public static void GoogleAccountLogout(Context context, final Frag_Mypage frag){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
            GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut(); // 재접속시 남아있는 구글 유저정보를 인식해 재로그인 하기 떄문에 방지를 위해 추가가
            frag.directToMainActivity(true);
        }
    }


    public static void accountResign(final Context context, final Frag_Mypage frag) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // 구글 연동 해제
            try {
                FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            frag.directToMainActivity(true);
                        }
                        else {
                            frag.directToMainActivity(false);
                        }
                    }
                }); // Firebase 인증 해제
                GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).revokeAccess(); // Google 계정 해제
            } catch (Exception e) {
                frag.directToMainActivity(false);
            }
        }
    }

}
