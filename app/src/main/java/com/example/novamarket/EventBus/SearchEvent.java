package com.example.novamarket.EventBus;

import java.util.ArrayList;

public class SearchEvent {
    public String content;
    public String type;

    public SearchEvent(String content, String type) {
        this.content = content;
        this.type = type;
    }
}
