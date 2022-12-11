package com.example.novamarket.Retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DTO_Address {
    @Expose
    @SerializedName("status")
    private ArrayList<DTO_Address> status;
    @Expose
    @SerializedName("code")
    private int code;
    @Expose
    @SerializedName("name")
    private String name;
    @Expose
    @SerializedName("message")
    private String message;
    @Expose
    @SerializedName("result")
    private ArrayList<DTO_Address> results;

}
