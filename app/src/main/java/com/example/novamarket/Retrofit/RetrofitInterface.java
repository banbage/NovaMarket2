package com.example.novamarket.Retrofit;


import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitInterface {

    @Headers({
            "X-NCP-APIGW-API-KEY-ID:dmw1yjiroc",
            "X-NCP-APIGW-API-KEY:w1aFydX0lMCVqrwzr3aZXKeauSb2Zi2BUCj9udor"
    })
    @GET("/map-reversegeocode/v2/gc")
    public Call<DTO_Address> getAddress(
            @Query("request") String request,
            @Query("sourcecrs") String sourcecrs,
            @Query("coords") String coords, // 입력 좌표
            @Query("orders") String orders, // 변환 작업
            @Query("output") String output // 출력 형식 Json
    );


    // 중복확인
    @FormUrlEncoded
    @POST("getUserId.php")
    Call<DTO_user> getUserId(
            @Field("user_id") String user_id
    );

    // 유저 정보 가져오기
    @FormUrlEncoded
    @POST("getUserInfo.php")
    Call<DTO_user> getUserInfo(
            @Field("user_id") String user_id
    );

    // 유저 로그인
    @FormUrlEncoded
    @POST("getUserLogin.php")
    Call<DTO_user> getUserLogin(
            @Field("user_id") String user_id,
            @Field("user_password") String user_password
    );

    // 회원가입
    @FormUrlEncoded
    @POST("writeUser.php")
    Call<DTO_user> writeUser(
            @Field("user_id") String user_id,
            @Field("user_name") String user_name,
            @Field("user_password") String user_password
    );

    // 로그인 API 회원가입
    @FormUrlEncoded
    @POST("writeAPI.php")
    Call<DTO_user> writeAPI(
            @Field("user_id") String user_id,
            @Field("user_name") String user_name,
            @Field("user_profile") String user_profile
    );

    // 회원 탈퇴
    @FormUrlEncoded
    @POST("deleteUser.php")
    Call<DTO_user> deleteUser(
            @Field("user_id") String user_id
    );

    // 회원정보 변경
    @Multipart
    @POST("updateUser.php")
    Call<DTO_user> updateUser(
            @Part("user_id") RequestBody user_id,
            @Part("user_name") RequestBody user_name,
            @Part MultipartBody.Part file
    );

    // 이메일 보내기
    @FormUrlEncoded
    @POST("sendmail.php")
    Call<DTO_user> sendMail(
            @Field("user_id") String user_id,
            @Field("user_password") String user_password
    );

    // 판매글 삭제
    @FormUrlEncoded
    @POST("deleteHome.php")
    Call<DTO_Home> deleteHome(
            @Field("user_id") String user_id,
            @Field("home_id") String home_id
    );


    // 판매글 등록
    @Multipart
    @POST("writeHome.php")
    Call<DTO_Home> writeHome(
            @Part("user_id") RequestBody user_id,
            @Part("title") RequestBody title,
            @Part("name") RequestBody name,
            @Part("cate") RequestBody cate,
            @Part("price") RequestBody price,
            @Part("propose") RequestBody propose,
            @Part("content") RequestBody content,
            @Part ArrayList<MultipartBody.Part> files
    );

    // 판매글 불러오기
    @FormUrlEncoded
    @POST("getHome.php")
    Call<ArrayList<DTO_Home>> getHome(
            @Field("page") int page,
            @Field("size") int size,
            @Field("search") String search,
            @Field("cate") String cate
    );

    @FormUrlEncoded
    @POST("getHomeRead.php")
    Call<DTO_Home> getHomeRead(
            @Field("home_id") String home_id,
            @Field("user_id") String user_id
    );

    @FormUrlEncoded
    @POST("writeHomeState.php")
    Call<DTO_Home> updateState(
            @Field("home_id") String home_id,
            @Field("state") String callback
    );

    @Multipart
    @POST("updateHome.php")
    Call<DTO_Home> updateHome(
            @Part("home_id") RequestBody home_id,
            @Part("user_id") RequestBody user_id,
            @Part("title") RequestBody title,
            @Part("name") RequestBody name,
            @Part("cate") RequestBody cate,
            @Part("price") RequestBody price,
            @Part("propose") RequestBody propose,
            @Part("content") RequestBody content,
            @Part ArrayList<MultipartBody.Part> files
    );

    @FormUrlEncoded
    @POST("deleteLike.php")
    Call<DTO_Home> deleteLike(
            @Field("id") String id,
            @Field("user") String user,
            @Field("type") String type
    );

    @FormUrlEncoded
    @POST("writeLike.php")
    Call<DTO_Home> writeLike(
            @Field("id") String id,
            @Field("user") String user,
            @Field("type") String type
    );

    @Multipart
    @POST("writeComm.php")
    Call<DTO_Comm> writeComm(
            @Part("user_id") RequestBody user_id,
            @Part("content") RequestBody content,
            @Part("cate") RequestBody cate,
            @Part ArrayList<MultipartBody.Part> files
    );

    @FormUrlEncoded
    @POST("getComm.php")
    Call<ArrayList<DTO_Comm>> getComm(
            @Field("page") int page,
            @Field("size") int size,
            @Field("search") String search,
            @Field("cate") String cate
    );

    @FormUrlEncoded
    @POST("writeComment.php")
    Call<DTO_Comment> writeComment(
            @Field("comm_id") String comm_id,
            @Field("user_id") String user_id,
            @Field("content") String content
    );

    @FormUrlEncoded
    @POST("getCommRead.php")
    Call<DTO_Comm> getCommRead(
            @Field("comm_id") String comm_id,
            @Field("user_id") String user_id,
            @Field("page") int page,
            @Field("size") int size
    );

    @Multipart
    @POST("updateComm.php")
    Call<DTO_Comm> updateComm(
            @Part("comm_id") RequestBody comm_id,
            @Part("user_id") RequestBody user_id,
            @Part("content") RequestBody content,
            @Part("cate") RequestBody cate,
            @Part ArrayList<MultipartBody.Part> files
    );

    @FormUrlEncoded
    @POST("deleteComm.php")
    Call<DTO_Comm> deleteComm(
            @Field("comm_id") String comm_id,
            @Field("user_id") String user_id
    );

    @FormUrlEncoded
    @POST("updateComment.php")
    Call<DTO_Comment> updateComment(
            @Field("comment_id") String comment_id,
            @Field("user_id") String user_id,
            @Field("comm_id") String comm_id,
            @Field("content") String content
    );

    @FormUrlEncoded
    @POST("updateReply.php")
    Call<DTO_Comment> updateReply(
            @Field("comment_id") String comment_id,
            @Field("user_id") String user_id,
            @Field("comm_id") String comm_id,
            @Field("content") String content
    );

    @FormUrlEncoded
    @POST("deleteComment.php")
    Call<DTO_Comment> deleteComment(
            @Field("comment_id") String comment_id,
            @Field("user_id") String user_id,
            @Field("comm_id") String comm_id
    );

    @FormUrlEncoded
    @POST("deleteReply.php")
    Call<DTO_Comment> deleteReply(
            @Field("comment_id") String comment_id,
            @Field("user_id") String user_id,
            @Field("comm_id") String comm_id,
            @Field("reply_group") String group
    );

    @FormUrlEncoded
    @POST("getReply.php")
    Call<DTO_Comment> getReply(
            @Field("comm_id") String comm_id,
            @Field("comment_id") String comment_id,
            @Field("user_id") String user_id,
            @Field("page") int page,
            @Field("size") int size
    );

    @FormUrlEncoded
    @POST("writeReply.php")
    Call<DTO_Comment> writeReply(
            @Field("comm_id") String comm_id,
            @Field("comment_id") String comment_id,
            @Field("user_id") String user_id,
            @Field("content") String content
    );

    @FormUrlEncoded
    @POST("getMoreReply.php")
    Call<DTO_Comment> getMoreReply(
            @Field("comm_id") String comm_id,
            @Field("comment_id") String comment_id
    );

    @FormUrlEncoded
    @POST("writeChat.php")
    Call<DTO_Chat> writeChat(
            @Field("home_id") String home_id,
            @Field("read_id") String writer_id,
            @Field("user_id") String user_id
    );

    @FormUrlEncoded
    @POST("getChat.php")
    Call<DTO_Chat> getChat(
            @Field("home_id") String home_id, // 작성자 정보 가져오기
            @Field("read_id") String read_id,
            @Field("user_id") String user_id, // 내 정보 가져오기기
            @Field("room_id") String room_id,
            @Field("size") int size,
            @Field("page") int page
    );

    @FormUrlEncoded
    @POST("getChatList.php")
    Call<DTO_Chat> getChatList(
            @Field("home_id") String home_id,
            @Field("user_id") String user_id,
            @Field("page") int page,
            @Field("size") int size
    );

    @FormUrlEncoded
    @POST("getChatJoin.php")
    Call<DTO_Chat> getChatJoin(
            @Field("user_id") String user_id,
            @Field("page") int page,
            @Field("size") int size
    );

    @Multipart
    @POST("writeImageMessage.php")
    Call<ArrayList<DTO_Chat>> writerImageMessage(
            @Part ArrayList<MultipartBody.Part> files);

    @FormUrlEncoded
    @POST("deleteChatRoom.php")
    Call<DTO_Chat> deleteChatRoom(
            @Field("user_id") String user_id,
            @Field("home_id") String home_id,
            @Field("room_id") String room_id
    );
}
