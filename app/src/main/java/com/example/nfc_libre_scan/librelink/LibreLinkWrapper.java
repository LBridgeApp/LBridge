package com.example.nfc_libre_scan.librelink;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.example.nfc_libre_scan.App;
import com.example.nfc_libre_scan.CriticalErrorHandler;
import com.example.nfc_libre_scan.Logger;

public class LibreLinkWrapper extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CriticalErrorHandler().setHandler();
        //startLibreLink();
    }

    /*private void startLibreLink(){
        final String packageName = "com.freestylelibre.app.ru";
        final String activityName = "com.librelink.app.ui.SplashActivity";
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        this.startActivity(intent);
        Logger.ok("LibreLink started");
    }*/
}