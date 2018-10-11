package com.five9.admin.digitalsignage.Common;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import com.five9.admin.digitalsignage.MyApplication;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceInfo {
    public static String getIpAddress(){
        String res = getIPAddress(true);
        if (TextUtils.isEmpty(res))
        	res = getIPAddress(false);
	    Log.d("abcxxx", "getIpAddress: " + res);
        return res;
    }

    public static String getMacAddress() {
    	String res = "";
	    try {
		    List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
		    for (NetworkInterface nif : all) {
			    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

			    byte[] macBytes = nif.getHardwareAddress();
			    if (macBytes == null) {
				    break;
			    }

			    StringBuilder res1 = new StringBuilder();
			    for (byte b : macBytes) {
				    String s = Integer.toHexString(b & 0xFF);
				    if (s.length() == 1)
				    	s = "0"+s;
				    res1.append(s).append(":");
			    }

			    if (res1.length() > 0) {
				    res1.deleteCharAt(res1.length() - 1);
			    }
			    res = res1.toString();
		    }
	    } catch (Exception ex) {
		    //handle exception
	    }
	    if (TextUtils.isEmpty(res)) {
	    	WifiManager manager = (WifiManager) MyApplication.getInstance().getApplicationContext()
			    .getSystemService(Context.WIFI_SERVICE);
	        WifiInfo info = manager.getConnectionInfo();
	        res = info.getMacAddress();
	    }
	    Log.d("abcxxx", "getMacAddress: " + res);
	    return res;
    }

	private static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress();
						boolean isIPv4 = sAddr.indexOf(':')<0;

						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
								return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
							}
						}
					}
				}
			}
		} catch (Exception ignored) { } // for now eat exceptions
		return "";
	}
}
