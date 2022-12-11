package com.example.novamarket.Class;

import com.google.gson.Gson;

import okhttp3.Response;

public class GsonConverter {

    public static String setLog (Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }
}
