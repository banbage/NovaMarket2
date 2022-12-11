package com.example.novamarket.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DTO_user {
    @Expose
    @SerializedName("user_id")
    private String user_id;
    @Expose
    @SerializedName("user_name")
    private String user_name;
    @Expose
    @SerializedName("user_profile")
    private String user_profile;
    @Expose
    @SerializedName("user_check")
    private boolean user_check;

    public boolean isUser_check() {
        return user_check;
    }

    public void setUser_check(boolean user_check) {
        this.user_check = user_check;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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
