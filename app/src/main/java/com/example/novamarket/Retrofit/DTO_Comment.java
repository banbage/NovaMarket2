package com.example.novamarket.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DTO_Comment {
    @Expose
    @SerializedName("name") private String name;
    @Expose
    @SerializedName("user_profile") private String user_profile;
    @Expose
    @SerializedName("like") private int like;
    @Expose
    @SerializedName("like_check") private boolean like_check;
    @Expose
    @SerializedName("user_id") private String user_id;
    @Expose
    @SerializedName("content") private String content;
    @Expose
    @SerializedName("date") private String date;
    @Expose
    @SerializedName("reply_group") private String group;
    @Expose
    @SerializedName("comm_id") private String comm_id;
    @Expose
    @SerializedName("total_comment") private int total_comment;
    @Expose
    @SerializedName("comment_id") private String comment_id;
    @Expose
    @SerializedName("total_reply") private int total_reply;
    @Expose
    @SerializedName("replies") private ArrayList<DTO_Comment> replies;

    public DTO_Comment(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_profile() {
        return user_profile;
    }

    public void setUser_profile(String user_profile) {
        this.user_profile = user_profile;
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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getComm_id() {
        return comm_id;
    }

    public void setComm_id(String comm_id) {
        this.comm_id = comm_id;
    }

    public int getTotal_comment() {
        return total_comment;
    }

    public void setTotal_comment(int total_comment) {
        this.total_comment = total_comment;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public int getTotal_reply() {
        return total_reply;
    }

    public void setTotal_reply(int total_reply) {
        this.total_reply = total_reply;
    }

    public ArrayList<DTO_Comment> getReplies() {
        return replies;
    }

    public void setReplies(ArrayList<DTO_Comment> replies) {
        this.replies = replies;
    }
}
