package com.example.polarh10.model;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Measurement {
    ArrayList<Integer> HRList;
    ArrayList<Integer> timeList;

    ArrayList<Integer> RRSList = new ArrayList<>();
    ArrayList<Long> timeSumList = new ArrayList<>();


    public Measurement(){
        /* CONSTRUCTOR */
    }

    public void setHRList(ArrayList<Integer> arrayList) {this.HRList = arrayList; }
    public void setTimeList(ArrayList<Integer> arrayList) {this.timeList = arrayList; }

    public ArrayList<Integer> getRRSList(){
        return RRSList;
    }

    public void addToRRSLists(Integer RRSvalue){
        if (timeSumList.size() < 1) {
            RRSList.add(0);
            timeSumList.add(0L);
        }
        RRSList.add(RRSvalue);
        timeSumList.add((long) timeSumList.get(timeSumList.size()-1) + RRSvalue );
    }

    public void writeCSV(String filename){
        /* Method to write the HR & time into a csv file and save it in the local strorage */
        File directoryDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        // /storage/emulated/0/Download
        File logDir = new File (directoryDownload, "PolarH10"); //Creates a new folder in DOWNLOAD directory
        logDir.mkdirs();
        File file = new File(logDir, filename);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file, false);
            for (int i = 0; i < HRList.size(); i++) {
                outputStream.write((timeList.get(i) + ",").getBytes());
                outputStream.write((HRList.get(i) + "\n").getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Write CSV", "something went wrong" + e);
        }
    }
}
