package com.five9.admin.digitalsignage.Common;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.five9.admin.digitalsignage.MyApplication;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Ulti {
    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");
    public static String getRootFolder(){
        try {
//            UsbManager manager = (UsbManager) MyApplication.getIntance().getSystemService(Context.USB_SERVICE);
//            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//            Toast.makeText(MyApplication.getIntance(), deviceIterator., Toast.LENGTH_LONG);
//            while(deviceIterator.hasNext()){
//                UsbDevice device = deviceIterator.next();
//                Toast.makeText(MyApplication.getIntance(), device.getDeviceName(), Toast.LENGTH_LONG);
//                //your code
//            }
            String res = Environment.getExternalStorageDirectory() + Constrant.ROOT_FOLDER;
            String list[] = getStorageDirectories();
            for (int i = 0; i < list.length; i++){
//            Log.d("xxx", "getRootFolder: " + list[i] + " " + new File(list[i]).exists());
                if (new File(list[i]).exists()){
                    res = list[i] + Constrant.ROOT_FOLDER;
                    break;
                }
            }
//            Toast.makeText(MyApplication.getIntance(), res, Toast.LENGTH_LONG).show();
            Log.d("xxx", "getRootFolder: " + res);
            return res;

        } catch (Exception ex){
            return  Environment.getExternalStorageDirectory() + Constrant.ROOT_FOLDER;
        }
    }

    public static String[] getStorageDirectories()
    {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if(TextUtils.isEmpty(rawEmulatedStorageTarget))
        {
            // Device has physical external storage; use plain paths.
            if(TextUtils.isEmpty(rawExternalStorage))
            {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            }
            else
            {
                rv.add(rawExternalStorage);
            }
        }
        else
        {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String[] folders = DIR_SEPORATOR.split(path);
            final String lastFolder = folders[folders.length - 1];
            boolean isDigit = false;
            try
            {
                Integer.valueOf(lastFolder);
                isDigit = true;
            }
            catch(NumberFormatException ignored)
            {
            }
            rawUserId = isDigit ? lastFolder : "";
            // /storage/emulated/0[1,2,...]
            if(TextUtils.isEmpty(rawUserId))
            {
                rv.add(rawEmulatedStorageTarget);
            }
            else
            {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
        {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }
}
