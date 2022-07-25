package com.example.novamarket.Class;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {

    // 현재 시간 String 으로 서버에 올리기
    public static String setDate() throws ParseException {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        return dateFormat.format(date);
    }

    // String to Date
   public static Date getDate(String from) throws ParseException {
        // "yyyy-MM-dd HH:mm:ss"
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN);
        return transFormat.parse(from);
    }

    // Date to String , pattern custom  output -> 2021년 05월 19일 등등
    public static String getString(Date date, String pattern) {
        SimpleDateFormat transFormat = new SimpleDateFormat(pattern, Locale.KOREA);
        return transFormat.format(date);
    }

    public static String resultDateToString(String date, String pattern) throws ParseException {
        Date tempDate = getDate(date);
        return getString(tempDate, pattern);
    }

    // 현재시간 가져오기
    public static Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    // String 을 시간으로?
    public static Long getStringToTime(String date) throws ParseException {
        return getDate(date).getTime();
    }

    // 언제 업로드했는지 보여준다.
    public static String getUploadMinuteTime(Long curTime, String dateTime,String pattern) throws ParseException {
        long boardTime = getStringToTime(dateTime);
        long result = (curTime - boardTime) / 60000;
        //분으로 표현,//시 + 분으로 표현
        if (result < 60) {
            return result + "분전";
        } else if (result > 1440) {
            return resultDateToString(dateTime, pattern);
        } else {
            int num = String.valueOf(result / 60.0).indexOf(".");
            String minute = String.valueOf(result / 60.0).substring(num);
            int newMinute = (int) (Double.parseDouble("0" + minute) * 60);
            return result / 60 + "시간" + newMinute + "분전";
        }
    }
}