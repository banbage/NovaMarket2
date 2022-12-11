package com.example.novamarket.Class;

import android.util.Log;

import com.example.novamarket.Retrofit.DTO_Chat;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

public class JsonMaker {
    private String code;
    private String json;
    private DTO_Chat msg;
    private Gson gson;

    public static String makeJson(DTO_Chat msg) {
        Gson gson = new Gson();
        return gson.toJson(msg);
    }

    // json String => DTO 로 변경해줌
    public static DTO_Chat makeDTO(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, DTO_Chat.class);
    }
}
