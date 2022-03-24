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
import android.widget.TextView;

import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.PolarBleApi.DeviceStreamingFeature;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.*;

import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.annotations.NonNull;

public class MainActivity extends AppCompatActivity {
    private PolarBleApi api;

    private String deviceId = "604C3D26"; //TODO insert device id
    Context context = this;
    int PERMISSION_ALL = 1;

    Button bConnect;
    TextView tv1;

    /*--------------------------- LOG -----------------------*/
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------- UI ----------*/
        bConnect = findViewById(R.id.connect);
        tv1 = findViewById(R.id.TV1);


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
        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean powered){
                Log.d("MyApp", "BLE power: " + powered);
            }

            @Override
            public void deviceConnected(@NonNull PolarDeviceInfo polarDeviceInfo){
                Log.d("MyApp", "CONNECTED: " + polarDeviceInfo.deviceId);
            }

            @Override
            public void deviceConnecting(@NonNull PolarDeviceInfo polarDeviceInfo){
                Log.d("MyApp", "CONNECTING: " + polarDeviceInfo.deviceId);
            }

            @Override
            public void deviceDisconnected(@NonNull PolarDeviceInfo polarDeviceInfo){
                Log.d("MyApp", "DISCONNECTED: " + polarDeviceInfo.deviceId);
            }

            @Override
            public void streamingFeaturesReady(@NonNull final String identifier,
                                               @NonNull final Set<DeviceStreamingFeature> features){
                for (PolarBleApi.DeviceStreamingFeature feature : features) {
                    Log.d("MyApp", "Streaming feature " + feature.toString() + " is ready");
                }
            }

            @Override
            public void hrFeatureReady(@NonNull String identifier){
                Log.d("MyApp", "HR READY: " + identifier);
            }

            @Override
            public void disInformationReceived(@NonNull String identifier, @NonNull UUID
                    uuid, @NonNull String value){
            }

            @Override
            public void batteryLevelReceived (@NonNull String identifier,int level){
            }

            @Override
            public void hrNotificationReceived (@NonNull String identifier, @NonNull PolarHrData data){
                Log.d("MyApp", "HR: " + data.hr);
            }

            @Override
            public void polarFtpFeatureReady (@NonNull String s){
            }
        });

        /*------ LISTENERS -----*/
        bConnect.setOnClickListener(v -> connect());


    }

    @Override
    public void onPause() {
        super.onPause();
        api.backgroundEntered();
    }

    @Override
    public void onResume() {
        super.onResume();
        api.foregroundEntered();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        api.shutDown();
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
            String sTV1 = "Connected to: " + deviceId;
            tv1.setText(sTV1);
        } catch (PolarInvalidArgument e){
            String msg = "mDeviceId=" + deviceId + "\nConnectToDevice: Bad argument:";
            Log.d(TAG, "    restart: " + msg);
            String sTV1b = "Couldn't connect to: " + deviceId;
            tv1.setText(sTV1b);
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