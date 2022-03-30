package com.example.polarh10;
/*
Application that records HR data into a csv-file
Elisa Perini
 */

import static android.view.View.GONE;
import static com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;

import static com.example.polarh10.utils.VisibilityChanger.*;
import static com.example.polarh10.utils.BluetoothPermissions.*;

import org.reactivestreams.Publisher;

public class MainActivity extends AppCompatActivity {
    private PolarBleApi api;

    private final String deviceId = "604C3D26";
    int PERMISSION_ALL = 1;

    /*---------- UI ------------*/
    Button bConnect, bStart, bStop, b3;
    TextView tvBT, tvDeviceConnection, tvHR, tvRRS;

    /*___________ TIMER __________*/
    Chronometer chronometer; // initiate chronometer
    long timeWhenStopped = 0; //keeps track of time in pause

    /*--------- SAVING ----------*/
    Measurement cMeasurement;
    ArrayList<Integer> HR = new ArrayList<>();
    ArrayList<Integer> time = new ArrayList<>();
    ArrayList<Integer> RRS = new ArrayList<>();
    boolean recording = false;

    /*------- ECG --------*/
    private Disposable mEcgDisposable;

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
        tvHR = findViewById(R.id.tv_hr);
        tvRRS = findViewById(R.id.tv_RRSms);
        chronometer = findViewById(R.id.chronometer);

        /*------- PERMISSIONS -------*/
        checkPermissions(this, MainActivity.this);

        /*-------- POLAR API --------*/
        polarApi();

        /*------- VISIBILITY ------*/
        set4ButtonVisibility(bConnect, View.VISIBLE, bStart, View.GONE, bStop, View.GONE, b3, GONE);

        /*------ LISTENERS -----*/
        bConnect.setOnClickListener(v -> connect());
        bStart.setOnClickListener(v -> start());
        bStop.setOnClickListener(v -> stop());
        b3.setOnClickListener(v -> recordECG());
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
                //api.searchForDevice();
                //api.autoConnectToDevice(-50, null, null).subscribe();
                String sTV1 = "Connected to: " + deviceId;
                tvDeviceConnection.setText(sTV1);
                set4ButtonVisibility(bConnect, View.GONE, bStart, View.VISIBLE, bStop, View.GONE, b3, GONE);
            /*} catch (PolarInvalidArgument e){
                Log.d(TAG, "error: " + e);
                String sTV1b = "Couldn't connect to: " + deviceId;
                tvDeviceConnection.setText(sTV1b); */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void start(){
        cMeasurement = new Measurement(); // instantiate class
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
        cMeasurement.getRRSList(); // to test if RRS was recorded
        cMeasurement.setHRList(HR);     // save whole HR list
        cMeasurement.setTimeList(time); // save time stamps
        cMeasurement.writeCSV("HR_recording.csv");
        Toast.makeText(getApplicationContext(), "Recording stopped and saved into a csv", Toast.LENGTH_SHORT).show();
    }

    public void recordECG(){
        // TODO: test if works
        //PolarSensorSetting setting =
        //api.startEcgStreaming(deviceId, setting);
        if (mEcgDisposable == null) {
            // Turns it on
            mEcgDisposable = (Disposable) api.requestStreamSettings(deviceId, DeviceStreamingFeature.ECG)
                    .toFlowable().flatMap((Function<PolarSensorSetting, Publisher<PolarEcgData>>)
                            sensorSetting -> api.startEcgStreaming(deviceId, sensorSetting.maxSettings()));
        }
        //Single<PolarSensorSetting> setting = api.requestStreamSettings(deviceId, DeviceStreamingFeature.ECG);
        //api.startEcgStreaming(deviceId, setting);

    }

    public void polarApi() {
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
                tvHR.setText(sTv2);

                String sTVRRS = "RRS: " + data.rrsMs.get(0) + " ms";
                tvRRS.setText(sTVRRS);
                if (recording) {
                    HR.add(data.hr);
                    long timeElapsed = SystemClock.elapsedRealtime() - chronometer.getBase();
                    int seconds = (int) timeElapsed/1000;
                    time.add(seconds);

                    if (data.rrsMs.size() == 1) cMeasurement.addToRRSLists(data.rrsMs.get(0));
                    else if (data.rrsMs.size() > 1) {
                        cMeasurement.addToRRSLists(data.rrsMs.get(0));
                        cMeasurement.addToRRSLists(data.rrsMs.get(1));
                    }

                }
            }

            @Override
            public void polarFtpFeatureReady (@NonNull String s){
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        Log.d(TAG, this.getClass().getSimpleName()  + "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ALL) {// All (Handle multiple)
            for (int i = 0; i < permissions.length; i++) {
                switch (permissions[i]) {
                    case Manifest.permission.ACCESS_COARSE_LOCATION:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: COARSE_LOCATION " + "granted");
                        } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: COARSE_LOCATION " + "denied");
                        }
                        break;
                    case Manifest.permission.ACCESS_FINE_LOCATION:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: FINE_LOCATION " + "granted");
                        } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: FINE_LOCATION " + "denied");
                        }
                        break;
                    case Manifest.permission.BLUETOOTH_SCAN:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_SCAN " + "granted");
                        } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_SCAN " + "denied");
                        }
                        break;
                    case Manifest.permission.BLUETOOTH_CONNECT:
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_CONNECT" + " " + "granted");
                        } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            Log.d(TAG, "REQ_ACCESS_PERMISSIONS: BLUETOOTH_CONNECT" + " " + "denied");
                        }
                        break;
                }
            }
        }
    }
}