package com.example.novamarket.Retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 필요한 변수 선언
    // 서버 주소
    private  static final  String BASE_URL = "http://3.39.57.242/NovaMarket/";
    private static  final  String NAVER_URL = "https://naveropenapi.apigw.ntruss.com/";
    private  static Retrofit retrofit;

    //레트로핏 객체에 데이터를 담아서 보내는 클라이언트 메소드
    public static Retrofit getApiClient() {
        //GSON 객체 생성 -> Gson Converter 를 사용
        Gson gson = new GsonBuilder()
                .setLenient() // Lenient : 관용 // RFC4627 의 규칙을 위반하는 JSON 은 처리하지 않지만, 허용의 폭을 넓혀줌
                .create();


        // 1. 인터셉터 객체 생성
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        // 2. 로그를 어떤 단계까지 표시할지 설정 - HTML 의 어디의 데이터를 가져올지
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 3. Retrofit 클라이언트에 넣을 OKHttpClient 객채 생성
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5,TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS) // 통신 타임아웃시간 설정
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(interceptor) // 인터셉터 설정
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL) // http 통신을 실행할 URL 주소
                .addConverterFactory(GsonConverterFactory.create(gson)) // 어떤 형태로 데이터를 parsing 할지 설정
                .client(client) // okHttp 객체 추가
                .build();

        return retrofit;
    }

    public static Retrofit getNaverApiClient(){
        //GSON 객체 생성 -> Gson Converter 를 사용
        Gson gson = new GsonBuilder()
                .setLenient() // Lenient : 관용 // RFC4627 의 규칙을 위반하는 JSON 은 처리하지 않지만, 허용의 폭을 넓혀줌
                .create();

        // 1. 인터셉터 객체 생성
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        // 2. 로그를 어떤 단계까지 표시할지 설정 - HTML 의 어디의 데이터를 가져올지
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // 3. Retrofit 클라이언트에 넣을 OKHttpClient 객채 생성
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5,TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS) // 통신 타임아웃시간 설정
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(interceptor) // 인터셉터 설정
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(NAVER_URL) // http 통신을 실행할 URL 주소
                .addConverterFactory(GsonConverterFactory.create(gson)) // 어떤 형태로 데이터를 parsing 할지 설정
                .client(client) // okHttp 객체 추가
                .build();

        return retrofit;
    }
}
