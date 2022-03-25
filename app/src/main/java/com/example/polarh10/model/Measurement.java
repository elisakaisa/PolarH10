package com.example.polarh10.model;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Measurement {
    ArrayList<Integer> HRList;
    ArrayList<Integer> timeList;

    public Measurement(){
        /* CONSTRUCTOR */
    }

    public void setHRList(ArrayList<Integer> arrayList) {this.HRList = arrayList; }
    public void setTimeList(ArrayList<Integer> arrayList) {this.timeList = arrayList; }

    public void writeCSV(String filename){
        File directoryDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        // /storage/emulated/0/Download
        File logDir = new File (directoryDownload, "PolarH10"); //Creates a new folder in DOWNLOAD directory
        logDir.mkdirs();
        File file = new File(logDir, filename);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, false);
            for (int i = 0; i < HRList.size(); i++) {
                //outputStream.write((timeList.get(i) + ",").getBytes());
                //outputStream.write((ewmaData.get(i) + ",").getBytes());
                outputStream.write((HRList.get(i) + "\n").getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Write CSV", "something went wrong" + e);
        }
    }
}
