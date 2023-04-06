package com.example.nfc_libre_scan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class LibreLinkDbRemover implements View.OnClickListener {
    private final Button libreLinkDatabaseRemoveBtn;
    private final Activity activity;
    private final RootLib rootLib;
    private final Logger logger;

    @SuppressLint("SdCardPath")
    private final String libreLinkDbPath = "/data/data/com.freestylelibre.app.ru/files/sas.db";
    LibreLinkDbRemover(Activity activity, Logger logger){
        this.activity = activity;
        this.logger = logger;
        this.rootLib = new RootLib(activity, logger);
        this.libreLinkDatabaseRemoveBtn = activity.findViewById(R.id.removeLibreLinkDatabase);
    }

    public void listen(){
        libreLinkDatabaseRemoveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        boolean rootIsGranted = rootLib.requestRootedProcess();
        if (!rootIsGranted) {
            logger.error("Root is not granted.");
            return;
        } else {
            logger.ok("Root granted");
        }

        try {
            rootLib.removeFile(libreLinkDbPath);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}
