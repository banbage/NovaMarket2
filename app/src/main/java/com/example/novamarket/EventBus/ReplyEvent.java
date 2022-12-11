package com.example.novamarket.EventBus;

public class ReplyEvent {
    public int pos = 0;
    public int total =0;

    public ReplyEvent(int pos,int total ) {
        this.pos = pos;
        this.total = total;
    }
}