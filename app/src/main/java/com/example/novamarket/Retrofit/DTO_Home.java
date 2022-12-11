package com.example.novamarket.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DTO_Home {
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("home_id")
    private String home_id;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("cate")
    private String cate;
    @Expose
    @SerializedName("price")
    private int price;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("content")
    private String content;
    @Expose
    @SerializedName("like")
    private int like;
    @Expose
    @SerializedName("like_check")
    private boolean like_check;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getHome_id() {
        return home_id;
    }

    public void setHome_id(String home_id) {
        this.home_id = home_id;
    }

    public String getTitle() {
        return title;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
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
}
