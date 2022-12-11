package com.example.novamarket.Class;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateConverter {
    public static final int SEC = 60;
    public static final int MIN = 60;
    public static final int HOUR = 24;
    public static final int DAY = 30;
    public static final int MONTH = 12;

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

    // String 을 Date 시간으로?
    public static Long getStringToTime(String date) throws ParseException {
        return getDate(date).getTime();
    }

    // 언제 업로드했는지 보여준다.
    public static String getUploadMinuteTime(String dateTime) throws ParseException {
        long boardTime = getStringToTime(dateTime); // 비교할 시간
        long result = (System.currentTimeMillis() - boardTime) / 1000; // 몇 초전
        //분으로 표현,//시 + 분으로 표현
        if (result < SEC) {
            return result + "초 전";
        } else if ((result /= SEC) < MIN) {
            return result + "분 전";
        } else if ((result /= MIN) < HOUR) {
            return result + "시간 전";
        } else if ((result /= HOUR) < DAY) {
            return result + "일 전";
        } else if ((result /= DAY) < MONTH) {
            return  result + "달 전";
        } else {
           return result + "년 전";
        }
    }
}