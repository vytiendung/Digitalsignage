package com.five9.admin.digitalsignage.Common;

public class Config {
    private static final String useName = "admin";
    private static final String pw = "Five9@123";
    private static final String END_POINT = "http://192.168.8.22:8000/";// "http://thinkzone.vn";

    public static String getServerEndpoint(){
        return END_POINT;
    }

    public static String getUseName(){
        return useName;
    }

    public static String getPw() {
        return pw;
    }
}
