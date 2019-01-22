package com.five9.admin.digitalsignage.Common;

public class Config {
    private static final String useName = "admin";
    private static final String pw = "Five9@123";
    private static final String END_POINT = "http://bms.thinkzone.vn";// "http://thinkzone.vn";

    public static String getServerEndpoint(){
        return DataStorage.getInstance().getString(Constant.END_POINT, END_POINT);
    }

    public static String getUseName(){
	    return DataStorage.getInstance().getString(Constant.USERNAME, useName);
    }

    public static String getPw() {
	    return DataStorage.getInstance().getString(Constant.PASSWORD, pw);
    }
}
