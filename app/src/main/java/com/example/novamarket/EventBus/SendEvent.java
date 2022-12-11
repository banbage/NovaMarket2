package com.example.novamarket.EventBus;


/*
 * 데이터를 주고 받기 위한 이벤트 버스 클래스 생성
 * */
public class SendEvent {
    public String content;
    public int pos;
    public boolean check;


    public SendEvent(boolean check) {
        this.check = check;
    }

    public SendEvent(int pos) {
        this.pos = pos;
    }
    public void SentEvent(int pos, boolean check){
        this.pos = pos;
        this.check = check;
    }

    public SendEvent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

}
