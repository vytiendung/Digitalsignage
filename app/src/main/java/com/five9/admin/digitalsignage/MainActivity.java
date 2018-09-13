package com.five9.admin.digitalsignage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private int REQUEST_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECEIVE_BOOT_COMPLETED
            };


            if (!hasPermissions(PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS );
            } else {
                startActivity(new Intent(this, PlayActivity.class));
                finish();
            }
        } else {
            startActivity(new Intent(this, PlayActivity.class));
            finish();
        }
    }

    public boolean hasPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean flag = true;
        for (int i = 0; i < grantResults.length;i++){
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                flag = false;
                 break;
            }
        }
        if (flag){
            startActivity(new Intent(this, PlayActivity.class));
            finish();
        } else {
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
