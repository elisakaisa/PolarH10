package com.example.polarh10;

import static com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.PolarBleApi.DeviceStreamingFeature;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.*;

import io.reactivex.rxjava3.annotations.NonNull;

public class MainActivity extends AppCompatActivity {
    private PolarBleApi api;

    private String deviceId = "604C3D26"; //TODO insert device id
    Context context = this;
    int PERMISSION_ALL = 1;

    Button bConnect;

    /*--------------------------- LOG -----------------------*/
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------- UI ----------*/
        bConnect = findViewById(R.id.connect);


        /*------- PERMISSIONS -------*/
        String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        api = defaultImplementation(getApplicationContext(),  PolarBleApi.ALL_FEATURES);

        /*------ LISTENERS -----*/
        bConnect.setOnClickListener(v -> connect());


    }

    private boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void connect(){
        try {
            api.connectToDevice(deviceId);
            Log.d(TAG, "connected to "+ deviceId);
        } catch (PolarInvalidArgument e){
            String msg = "mDeviceId=" + deviceId + "\nConnectToDevice: Bad argument:";
            Log.d(TAG, "    restart: " + msg);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        Log.d(TAG, this.getClass().getSimpleName()  + "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {// All (Handle multiple)
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.
                        permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: COARSE_LOCATION " + "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: COARSE_LOCATION " + "denied");
                    }
                } else if (permissions[i].equals(Manifest.
                        permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: FINE_LOCATION " + "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: FINE_LOCATION " + "denied");
                    }
                } else if (permissions[i].equals(Manifest.
                        permission.BLUETOOTH_SCAN)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_SCAN " + "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_SCAN " + "denied");
                    }
                } else if (permissions[i].equals(Manifest.
                        permission.BLUETOOTH_CONNECT)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_CONNECT" + " " + "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_CONNECT" +  " " + "denied");
                    }
                }
            }
        }

    }
}