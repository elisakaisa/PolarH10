package com.example.polarh10.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class BluetoothPermissions {

    static int PERMISSION_ALL = 1;

    /*---------- BLUETOOTH -----------*/
    public static void checkPermissions(Context context, Activity activity){
        String[] PERMISSIONS = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PERMISSIONS = new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    private static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
