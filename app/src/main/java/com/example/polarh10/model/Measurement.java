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
    public ArrayList<Integer> getTimeList(){
        return timeList;
    }
    public ArrayList<Integer> getHRList(){
        return HRList;
    }

    public void addToRRSLists(Integer RRSvalue){
        if (timeSumList.size() < 1) {
            RRSList.add(0);
            timeSumList.add(0L);
        }
        RRSList.add(RRSvalue);
        timeSumList.add((long) timeSumList.get(timeSumList.size()-1) + RRSvalue );
    }
}
