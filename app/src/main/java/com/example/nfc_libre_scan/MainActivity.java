package com.example.nfc_libre_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.nfc_libre_scan.libre.LibreNFC;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.example.nfc_libre_scan.librelink.LibreLink;
import com.oop1.CurrentBg;
import com.oop1.GlucoseUnit;
import com.oop1.HistoricBg;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LibreMessageListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, LogListener {
    private TextView currentBgView;
    private TextView bgHistoryView;
    private TextView logTextView;
    private LibreMessage libreMessage;
    private LibreLink libreLink;
    private PermissionLib permissionLib;
    private GlucoseUnit glucoseUnit = GlucoseUnit.MMOL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logTextView = this.findViewById(R.id.logTextView);
        Logger.setLoggerListener(this);

        LibreNFC libreNFC = new LibreNFC(this);
        libreNFC.listenSensor();
        libreNFC.setLibreMessageListener(this);

        currentBgView = this.findViewById(R.id.currentBgView);
        bgHistoryView = this.findViewById(R.id.bgHistoryView);

        Button sugarAddingBtn = this.findViewById(R.id.sugarAddingBtn);
        Button clearLogWindowBtn = this.findViewById(R.id.clearLogWindowBtn);

        clearLogWindowBtn.setOnClickListener(this);

        if (glucoseUnit == GlucoseUnit.MMOL) {
            ((RadioButton) this.findViewById(R.id.mmolGlucoseUnit)).setChecked(true);
        }
        if (glucoseUnit == GlucoseUnit.MGDL) {
            ((RadioButton) this.findViewById(R.id.mgdlGlucoseUnit)).setChecked(true);
        }

        RadioGroup glucoseUnitRadioGroup = this.findViewById(R.id.glucose_unit);
        glucoseUnitRadioGroup.setOnCheckedChangeListener(this);

        permissionLib = new PermissionLib(this);

        try {
            libreLink = new LibreLink(this);
            libreLink.listenLibreMessages(libreNFC);
            sugarAddingBtn.setOnClickListener(this);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void showBG() {
        if (libreMessage == null) {
            return;
        }

        DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("HH:mm").toFormatter();

        CurrentBg currentBgObject;
        HistoricBg[] historicBgArray;
        try {
            currentBgObject = libreMessage.getCurrentBg().convertBG(this.glucoseUnit);
            double currentBG = currentBgObject.getBG();
            String trend = currentBgObject.getCurrentTrend().toString();
            String glucoseUnit = currentBgObject.getGlucoseUnit().getString();
            historicBgArray = libreMessage.getHistoricBgs();

            String localTime = currentBgObject.getSensorTimeAsLocalTime().format(dtf);

            String textString = String.format(Locale.US, "Time: %s\nCurrent BG: %.1f %s\nTrend: %s", localTime, currentBG, glucoseUnit, trend);
            this.runOnUiThread(() -> this.currentBgView.setText(textString));

            StringBuilder historicBgBuilder = new StringBuilder();
            for (HistoricBg historicBg : historicBgArray) {
                historicBg = historicBg.convertBG(this.glucoseUnit);
                double bg = historicBg.getBG();
                glucoseUnit = historicBg.getGlucoseUnit().getString();
                localTime = historicBg.getSensorTimeAsLocalTime().format(dtf);

                final String str = String.format(Locale.US, "Time: %s, BG: %.1f %s\n", localTime, bg, glucoseUnit);
                historicBgBuilder.append(str);
            }
            this.runOnUiThread(() -> this.bgHistoryView.setText(historicBgBuilder));

        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    public void libreMessageReceived(LibreMessage message) {
        this.libreMessage = message;
        this.showBG();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.mmolGlucoseUnit) {
            this.glucoseUnit = GlucoseUnit.MMOL;
        }
        if (checkedId == R.id.mgdlGlucoseUnit) {
            this.glucoseUnit = GlucoseUnit.MGDL;
        }
        this.showBG();
    }

    @Override
    public void logReceived(Logger.LogRecord log) {
        String b = String.format("%s\n", log.toShortString()) +
                logTextView.getText();
        String[] logs = b.split("\n");
        StringBuilder b2 = new StringBuilder();
        int logLinesCount = Math.min(logs.length, 10);
        for (int i = 0; i < logLinesCount; i++) {
            b2.append(String.format("%s\n", logs[i]));
        }
        this.runOnUiThread(() -> logTextView.setText(b2));
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.clearLogWindowBtn) {
            this.runOnUiThread(() -> logTextView.setText(null));
        } else if (v.getId() == R.id.sugarAddingBtn) {
            try {
                libreLink.addLastScanToDatabase();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.open_logs) {
            Intent intent = new Intent(this, LogActivity.class);
            this.startActivity(intent);
        } else if (item.getItemId() == R.id.set_draw_overlay_permission) {
            permissionLib.setDrawOverlayActivity();
        }
        else if(item.getItemId() == R.id.startService){
            WebService.startService();
        }
        else if(item.getItemId() == R.id.stopService){
            WebService.stopService();
        }
        else if(item.getItemId() == R.id.goto_developing_options){
            Intent intent = new Intent(this, DevelopingActivity.class);
            this.startActivity(intent);
        }
        else if(item.getItemId() == R.id.exit){
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }
}