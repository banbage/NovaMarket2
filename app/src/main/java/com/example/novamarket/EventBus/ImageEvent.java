package com.example.novamarket.EventBus;

public class ImageEvent {
    public boolean check = false;
    public String where = null;
    public String url = null;
    public double lat = 0;
    public double log = 0;

    public ImageEvent(boolean check) {
        this.check = check;
    }

    public ImageEvent( String where, String url, double lat, double log) {
        this.where = where;
        this.url = url;
        this.lat = lat;
        this.log = log;
    }

    public ImageEvent(String where) {
        this.where = where;
    }
}
