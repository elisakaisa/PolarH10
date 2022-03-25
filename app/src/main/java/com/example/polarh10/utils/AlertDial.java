package com.example.polarh10.utils;

/*
class used by MainActivity to get alert Dialogs
Elisa Perini
 */
import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AlertDial extends AppCompatActivity {

    // Alert dialogs for error messages
    public AlertDialog createMsgDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", (dialog, id) -> {});
        return builder.create();
    }
}
