package com.example.polarh10.utils;
/*
Utility class to set the visibility of buttons and text views
Elisa Perini
 */

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class VisibilityChanger {

    public static void set4ButtonVisibility(Button b1, int v1, Button b2, int v2, Button b3, int v3, Button b4, int v4){
        b1.setVisibility(v1);
        b2.setVisibility(v2);
        b3.setVisibility(v3);
        b4.setVisibility(v4);
    }
    //change visibility of three views
    public static void set3ButtonVisibility(Button b1, int v1, Button b2, int v2, Button b3, int v3){
        b1.setVisibility(v1);
        b2.setVisibility(v2);
        b3.setVisibility(v3);
    }
    public static void set2ButtonVisibility(Button b1, int v1, Button b2, int v2){
        b1.setVisibility(v1);
        b2.setVisibility(v2);
    }

    public static void setViewsInvisible(TextView v1, TextView v2){
        v1.setVisibility(View.INVISIBLE);
        v2.setVisibility(View.INVISIBLE);
    }

    public static void setViewsVisible(TextView v1, TextView v2){
        v1.setVisibility(View.VISIBLE);
        v2.setVisibility(View.VISIBLE);
    }
}
