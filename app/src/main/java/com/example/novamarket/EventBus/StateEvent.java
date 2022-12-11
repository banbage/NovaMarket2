package com.example.novamarket.EventBus;

public class StateEvent {
    public String state;

    public StateEvent(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
