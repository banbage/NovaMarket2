package com.example.novamarket.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DTO_Comm {
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("comm_id")
    private String comm_id;
    @Expose
    @SerializedName("cate")
    private String cate;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("like")
    private int like;
    @Expose
    @SerializedName("total_comment")
    private int total_comment;
    @Expose
    @SerializedName("total_like")
    private int total_like;
    @Expose
    @SerializedName("like_check")
    private boolean like_check;
    @Expose
    @SerializedName("like_check_comment")
    private boolean like_check_comment;
    @Expose
    @SerializedName("like_reply_check")
    private boolean like_reply_check;
    @Expose
    @SerializedName("chat")
    private int chat;
    @Expose
    @SerializedName("image")
    private String image;
    @Expose
    @SerializedName("images")
    private ArrayList<DTO_Image> images;
    @Expose
    @SerializedName("date")
    private String date;
    @Expose
    @SerializedName("user_name")
    private String user_name;
    @Expose
    @SerializedName("user_profile")
    private String user_profile;
    @Expose
    @SerializedName("total_reply")
    private int total_reply;
    @Expose
    @SerializedName("comments")
    private ArrayList<DTO_Comment> comments;
    public int getTotal_like() {
        return total_like;
    }

    public int getTotal_reply() {
        return total_reply;
    }

    public void setTotal_reply(int total_reply) {
        this.total_reply = total_reply;
    }

    public void setTotal_like(int total_like) {
        this.total_like = total_like;
    }

    public int getTotal_comment() {
        return total_comment;
    }

    public void setTotal_comment(int total_comment) {
        this.total_comment = total_comment;
    }

    public ArrayList<DTO_Comment> getComments() {
        return comments;
    }

    public boolean isLike_check_comment() {
        return like_check_comment;
    }

    public void setLike_check_comment(boolean like_check_comment) {
        this.like_check_comment = like_check_comment;
    }

    public boolean isLike_reply_check() {
        return like_reply_check;
    }

    public void setLike_reply_check(boolean like_reply_check) {
        this.like_reply_check = like_reply_check;
    }

    public void setComments(ArrayList<DTO_Comment> comments) {
        this.comments = comments;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getComm_id() {
        return comm_id;
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
    }


    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public boolean isLike_check() {
        return like_check;
    }

    public void setLike_check(boolean like_check) {
        this.like_check = like_check;
    }

    public int getChat() {
        return chat;
    }

    public void setChat(int chat) {
        this.chat = chat;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public ArrayList<DTO_Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<DTO_Image> images) {
        this.images = images;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_profile() {
        return user_profile;
    }

    public void setUser_profile(String user_profile) {
        this.user_profile = user_profile;
    }
}
