package com.example.novamarket.EventBus;

public class HomeEvent {
    public String home_id;
    public boolean reset = false;

    public HomeEvent(String home_id) {
        this.home_id = home_id;
    }

    public HomeEvent(boolean resetData) {
        this.reset = resetData;
    }
}
