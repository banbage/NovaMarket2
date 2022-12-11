package com.example.novamarket.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DTO_Chat {
    @Expose
    @SerializedName("success") private String success;
    // chat_room
    @Expose
    @SerializedName("room_id") String room_id; // 채팅방 고유값
    @Expose
    @SerializedName("home_id") String home_id; // 어떤 매물의 채팅방인지
    @Expose
    @SerializedName("room_state") String room_state; // 현재 활성화 되었는지 아닌지
    // chat_join
    @Expose
    @SerializedName("join_id") String join_id; // 채팅하는 유저의 고유값
    @Expose
    @SerializedName("my_id") String my_id; // 내 정보
    @Expose
    @SerializedName("user_id") String user_id; // 상대방정보
    @Expose
    @SerializedName("last_msg") String last_msg; // 채팅방의 마지막 메세지
    @Expose
    @SerializedName("room_count") int room_count; // 채팅방 안읽은 메세지 수
    @Expose
    @SerializedName("joins")
    ArrayList<DTO_Chat> joins;
    // chat_message
    @Expose
    @SerializedName("messages")
    ArrayList<DTO_Chat> messages; // 기존의 채팅 메세지
    @Expose
    @SerializedName("msg_profile") String msg_profile; // 유저 프로필사진
    @Expose
    @SerializedName("message_id") String message_id;
    @Expose
    @SerializedName("msg_code") String msg_code; // 서버에서 어떠한 처리를 할지
    @Expose
    @SerializedName("msg") String msg; // 채팅 내용
    @Expose
    @SerializedName("msg_image") String msg_image; // 이미지 전송시 보여줄 이미지 및 장소 약속시 보여줄 이미지
    @Expose
    @SerializedName("msg_date") String msg_date; // 메세지 전송 시간
    @Expose
    @SerializedName("msg_type") String msg_type; // 메세지 layout type 결정
    @Expose
    @SerializedName("msg_state") String msg_state; // 메세지 읽음 표시
    @Expose
    @SerializedName("msg_lat") String msg_lat; // 약속시 위도 Y축
    @Expose
    @SerializedName("msg_log") String msg_log; // 약속시 경도 X축
    @Expose
    @SerializedName("msg_name") String msg_name; // 채팅 전송시 보여줄 이름
    // chat_read
    @Expose
    @SerializedName("writer_id") String writer_id; // 채팅방 글쓴이가 누구인지
    @Expose
    @SerializedName("writer_name") String writer_name; // 채팅방 이름 띄워주기
    @Expose
    @SerializedName("home_image") String home_image; // 상품이미지
    @Expose
    @SerializedName("home_title") String home_title; // 상품 제목
    @Expose
    @SerializedName("home_price") String home_price; // 상품 가격
    @Expose
    @SerializedName("drawable") int drawable;

    /**
     * @param msg_profile : 유저 이미지
     * @param room_id : 방 번호
     * @param user_id : 누가 보낸 메세지인지
     * @param msg_name : 표기할 작성자 이름
     * @param msg_code : 서버에서 어떻게 처리할지
     * @param msg : 채팅 내용
     * @param msg_image : 이미지를 보낼 경우 내용
     * @param msg_date : 메세지 전송한 시간
     * @param msg_type : 메세지를 type layout 에 담을지 결정
     * @param msg_state : 메세지 읽음 않읽음 표시
     * @param msg_lat : 약속 장소 전송시 위도
     * @param msg_log : 약속 장소 전송시 경도
     * */
    public DTO_Chat(String msg_profile,String room_id,String user_id, String msg_name, String msg_code, String msg, String msg_image, String msg_date, String msg_type, String msg_state, String msg_lat, String msg_log, String home_id) {
        this.msg_profile = msg_profile;
        this.msg_code = msg_code;
        this.room_id = room_id;
        this.user_id = user_id;
        this.msg_name = msg_name;
        this.msg = msg;
        this.msg_image = msg_image;
        this.msg_date = msg_date;
        this.msg_type = msg_type;
        this.msg_state = msg_state;
        this.msg_lat = msg_lat;
        this.msg_log = msg_log;
        this.home_id = home_id;
    }

    public DTO_Chat(String msg_name, int drawable) {
        this.msg_name = msg_name;
        this.drawable = drawable;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getWriter_name() {
        return writer_name;
    }

    public void setWriter_name(String writer_name) {
        this.writer_name = writer_name;
    }

    public ArrayList<DTO_Chat> getJoins() {
        return joins;
    }

    public void setJoins(ArrayList<DTO_Chat> joins) {
        this.joins = joins;
    }

    public ArrayList<DTO_Chat> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<DTO_Chat> messages) {
        this.messages = messages;
    }

    public String getMsg_profile() {
        return msg_profile;
    }

    public void setMsg_profile(String msg_profile) {
        this.msg_profile = msg_profile;
    }

    public String getMsg_name() {
        return msg_name;
    }

    public void setMsg_name(String msg_name) {
        this.msg_name = msg_name;
    }

    public String getWriter_id() {
        return writer_id;
    }

    public void setWriter_id(String writer_id) {
        this.writer_id = writer_id;
    }

    public String getHome_image() {
        return home_image;
    }

    public void setHome_image(String home_image) {
        this.home_image = home_image;
    }

    public String getHome_title() {
        return home_title;
    }

    public void setHome_title(String home_title) {
        this.home_title = home_title;
    }

    public String getHome_price() {
        return home_price;
    }

    public void setHome_price(String home_price) {
        this.home_price = home_price;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getHome_id() {
        return home_id;
    }

    public void setHome_id(String home_id) {
        this.home_id = home_id;
    }

    public String getRoom_state() {
        return room_state;
    }

    public void setRoom_state(String room_state) {
        this.room_state = room_state;
    }

    public String getJoin_id() {
        return join_id;
    }

    public void setJoin_id(String join_id) {
        this.join_id = join_id;
    }

    public String getMy_id() {
        return my_id;
    }

    public void setMy_id(String my_id) {
        this.my_id = my_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLast_msg() {
        return last_msg;
    }

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    public int getRoom_count() {
        return room_count;
    }

    public void setRoom_count(int room_count) {
        this.room_count = room_count;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMsg_code() {
        return msg_code;
    }

    public void setMsg_code(String msg_code) {
        this.msg_code = msg_code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg_image() {
        return msg_image;
    }

    public void setMsg_image(String msg_image) {
        this.msg_image = msg_image;
    }

    public String getMsg_date() {
        return msg_date;
    }

    public void setMsg_date(String msg_date) {
        this.msg_date = msg_date;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getMsg_state() {
        return msg_state;
    }

    public void setMsg_state(String msg_state) {
        this.msg_state = msg_state;
    }

    public String getMsg_lat() {
        return msg_lat;
    }

    public void setMsg_lat(String msg_lat) {
        this.msg_lat = msg_lat;
    }

    public String getMsg_log() {
        return msg_log;
    }

    public void setMsg_log(String msg_log) {
        this.msg_log = msg_log;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

}
