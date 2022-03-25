package com.example.polarh10.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog.Builder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AlertDial extends AppCompatActivity {

    private Context context;

    // Alert dialogs for error messages
    public AlertDialog createMsgDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", (dialog, id) -> {});
        return builder.create();
    }
}
