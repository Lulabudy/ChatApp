package com.example.chatappprueba3.ui.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.chatappprueba3.R;


public class LoadingDialog {

    Activity activity;
    AlertDialog dialog;

    LoadingDialog(Activity activity){
        this.activity = activity;
    }

    void start(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null))
        .setCancelable(false);

        this.dialog = builder.create();
        this.dialog.show();
    }

    void dismissDialog(){
        this.dialog.dismiss();
    }
}
