package com.example.polarh10;

import static android.view.View.GONE;
import static com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.polarh10.model.Measurement;
import com.example.polarh10.utils.AlertDial;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApi.DeviceStreamingFeature;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import io.reactivex.rxjava3.annotations.NonNull;

import static com.example.polarh10.utils.VisibilityChanger.*;

public class MainActivity extends AppCompatActivity {
    private PolarBleApi api;

    private String deviceId = "604C3D26"; //TODO insert device id
    Context context = this;
    int PERMISSION_ALL = 1;

    Button bConnect, bStart, bStop, b3;
    TextView tvBT, tvDeviceConnection, tv2;

    /*___________ TIMER __________*/
    Chronometer chronometer; // initiate chronometer
    long timeWhenStopped = 0; //keeps track of time in pause

    /*--------- SAVING ----------*/
    Measurement cMeasurement;
    ArrayList<Integer> HR = new ArrayList<>();
    boolean recording = false;

    /*--------------------------- LOG -----------------------*/
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*---------- UI ----------*/
        bConnect = findViewById(R.id.connect);
        bStart = findViewById(R.id.b_start);
        bStop = findViewById(R.id.b_stop);
        b3 = findViewById(R.id.button3);
        tvBT = findViewById(R.id.BTconnection);
        tvDeviceConnection = findViewById(R.id.deviceConnection);
        tv2 = findViewById(R.id.tv_hr);
        chronometer = findViewById(R.id.chronometer);


        /*------- PERMISSIONS -------*/
        checkPermissions();

        /*-------- POLAR API --------*/
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
                String sTv2 = "HR: " + data.hr;
                tv2.setText(sTv2);
                if (recording) HR.add(data.hr);
            }

            @Override
            public void polarFtpFeatureReady (@NonNull String s){
            }
        });

        /*------- VISIBILITY ------*/
        set4ButtonVisibility(bConnect, View.VISIBLE, bStart, View.GONE, bStop, View.GONE, b3, GONE);

        /*------ LISTENERS -----*/
        bConnect.setOnClickListener(v -> connect());
        bStart.setOnClickListener(v -> start());
        bStop.setOnClickListener(v -> stop());


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


    public void connect(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            new AlertDial().createMsgDialog(MainActivity.this, "No bluetooth", "Device does not support Bluetooth").show();
            tvBT.setText(R.string.BTnotSupported);
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth is not enabled :)
            new AlertDial().createMsgDialog(MainActivity.this, "No bluetooth", "Turn Bluetooth on").show();
            tvBT.setText(R.string.BToff);
        } else {
            tvBT.setText(R.string.BTon);
            try {
                api.connectToDevice(deviceId); //CONNECT
                Log.d(TAG, "connected to "+ deviceId);
                String sTV1 = "Connected to: " + deviceId;
                tvDeviceConnection.setText(sTV1);
                set4ButtonVisibility(bConnect, View.GONE, bStart, View.VISIBLE, bStop, View.GONE, b3, GONE);
            } catch (PolarInvalidArgument e){
                String msg = "mDeviceId=" + deviceId + "\nConnectToDevice: Bad argument:";
                Log.d(TAG, "    restart: " + msg);
                String sTV1b = "Couldn't connect to: " + deviceId;
                tvDeviceConnection.setText(sTV1b);
            }
        }

    }

    public void start(){
        cMeasurement = new Measurement();
        // chronometer
        chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        chronometer.start();
        recording = true;
        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_SHORT).show();
        set4ButtonVisibility(bConnect, View.GONE, bStart, View.GONE, bStop, View.VISIBLE, b3, GONE);
    }

    public void stop(){
        chronometer.stop();
        recording = false;
        set4ButtonVisibility(bConnect, View.GONE, bStart, View.VISIBLE, bStop, GONE, b3, GONE);
        cMeasurement.setHRList(HR);
        cMeasurement.writeCSV("HRrecording.csv");
        Toast.makeText(getApplicationContext(), "Recording stopped and saved into a csv", Toast.LENGTH_SHORT).show();
    }

    /*---------- BLUETOOTH -----------*/
    public void checkPermissions(){
        String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public void checkBluetooth(){

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        Log.d(TAG, this.getClass().getSimpleName()  + "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {// All (Handle multiple)
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: COARSE_LOCATION " + "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: COARSE_LOCATION " + "denied");
                    }
                } else if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: FINE_LOCATION " + "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: FINE_LOCATION " + "denied");
                    }
                } else if (permissions[i].equals(Manifest.permission.BLUETOOTH_SCAN)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_SCAN " + "granted");
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_SCAN " + "denied");
                    }
                } else if (permissions[i].equals(Manifest.permission.BLUETOOTH_CONNECT)) {
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