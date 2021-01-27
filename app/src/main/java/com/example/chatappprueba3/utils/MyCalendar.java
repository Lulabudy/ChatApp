package com.example.chatappprueba3.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyCalendar {

    public static Calendar c;
    private static SimpleDateFormat timeFormat;
    private static SimpleDateFormat dateFormat;

    public static String getTime(){
        c = Calendar.getInstance();
        timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(c.getTime());
    }

    public static String getDate(){
        c = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(c.getTime());
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:sss'Z'");
        String date = ISO_8601_FORMAT.format(new Date());
        return date;
    }
}
