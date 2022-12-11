package com.example.novamarket.Class;

import android.app.Application;
import android.content.Context;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

import java.util.ArrayList;
import java.util.List;

public class GlobalHelper extends Application { // 앱 전체 권한 상속
    /*
    * 1. SDK 초기화
    * 2. 계정 정보 관리
    * */

    // 변수를 Main Memory 에 저장하겠다고 명시하는것
    // volatile 변수 를 사용하지 않으면 성능 향상을 위해 Main Memory 에서 읽은 변수 값을 CPU Cache 에 저장한다.
    // Multi Thread 환경에서 Thread 가 변수 값을 읽어올 때 각각의 CPU Cache 에 저장하는 값이 다르기 때문에 변수 값 불일치 발생
    private static volatile GlobalHelper mInstance = null;
    private static List<String> mGlobalUserLoginInfo = new ArrayList<>();
    private static String snsNumber;

    // 카카오 로그인 권한
    public static GlobalHelper getGlobalApplicationContext() {
        if (mInstance == null) { // GlobalHelper 가 null 생성 안할때
            throw new IllegalStateException("This Application does not GlobalAuthHelper");
        }
        return mInstance;
    }

    // 로그인시 프로필 데이터 관리
    public static List<String> getGlobalUserLoginInfo(){
        return mGlobalUserLoginInfo;
    }

    // 카카오 로그인 데이터 세팅
    public static void setGlobalUserLoginInfo(List<String> userLoginInfo) {
        mGlobalUserLoginInfo = userLoginInfo;
    }

    // 생성 했을때 SDK를 초기화 한다.
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this; // 생성자 만들면 내 자신 넣기
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    /*
    KakaoSDK 를 사용하기 전 초기화를 해야한다.
    Application 을 상속받는 GlobalHelper 클래스를 생성하여 앱 수준에서 관리하기

    앱 수준에서 GlobalHelper.class 를 인식하기 위해 AndroidManifest.xml 을 수정해야 한다.
    로그인을 위해 인터넷 권한을 부여하고 Strings.xml 에 저장했던 kakao_app_key 를 메타 데이터로 넣는다. 그리고 생성해둔 GlobalHelper 클래스를 application 의 name 태그에 넣어준다
    */
    public class KakaoSDKAdapter extends KakaoAdapter {
        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[]{AuthType.KAKAO_LOGIN_ALL};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return GlobalHelper.getGlobalApplicationContext();
                }
            };
        }
    }
}
