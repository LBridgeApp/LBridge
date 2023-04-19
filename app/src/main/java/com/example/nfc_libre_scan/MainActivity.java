package com.example.nfc_libre_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.nfc_libre_scan.libre.Libre;
import com.example.nfc_libre_scan.libre.LibreMessage;
import com.oop1.CurrentBg;
import com.oop1.GlucoseUnit;
import com.oop1.HistoricBg;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnLibreMessageListener, RadioGroup.OnCheckedChangeListener, OnLogListener {
    private TextView currentBgView;
    private TextView bgHistoryView;
    private TextView logTextView;
    private LibreMessage libreMessage;
    private GlucoseUnit glucoseUnit = GlucoseUnit.MMOL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CriticalErrorHandler(this.getApplicationContext()));
        setContentView(R.layout.activity_main);

        logTextView = this.findViewById(R.id.logTextView);
        Logger.setLoggerListener(this);

        AppTester appTester = new AppTester(this);
        boolean testsPassed = appTester.runTests();
        if (testsPassed) {
            Logger.ok("Tests passed!");
        }
        if (!testsPassed) {
            Logger.error("Tests failed! Exiting.");
            return;
        }

        Libre libre = new Libre(this);
        libre.listenSensor();
        libre.setLibreListener(this);

        LibreLink libreLink = new LibreLink(this);
        libreLink.listenLibreMessages(libre);

        currentBgView = this.findViewById(R.id.currentBgView);
        bgHistoryView = this.findViewById(R.id.bgHistoryView);

        Button sugarAddingBtn = this.findViewById(R.id.sugarAddingBtn);
        Button databaseRemovingBtn = this.findViewById(R.id.removeLibrelinkDB);

        sugarAddingBtn.setOnClickListener(libreLink);
        databaseRemovingBtn.setOnClickListener(libreLink);


        if (glucoseUnit == GlucoseUnit.MMOL) {
            ((RadioButton) this.findViewById(R.id.mmolGlucoseUnit)).setChecked(true);
        }
        if (glucoseUnit == GlucoseUnit.MGDL) {
            ((RadioButton) this.findViewById(R.id.mgdlGlucoseUnit)).setChecked(true);
        }

        RadioGroup glucoseUnitRadioGroup = this.findViewById(R.id.glucose_unit);
        glucoseUnitRadioGroup.setOnCheckedChangeListener(this);

        boolean startedFirstTime = WebService.startService(this);
        if(!startedFirstTime){
            Logger.error("WebService already running.");
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
            currentBgObject = libreMessage.getCurrentBgObject().convertBG(this.glucoseUnit);
            double currentBG = currentBgObject.getBG();
            String trend = currentBgObject.getCurrentTrend().toString();
            String glucoseUnit = currentBgObject.getGlucoseUnit().getString();
            historicBgArray = libreMessage.getHistoricBgArray();

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
            Logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
        }
    }

    @Override
    public void onLibreMessageReceived(LibreMessage message) {
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
    public void onLogReceived(String log) {
        this.runOnUiThread(() -> logTextView.append("\n" + log + "\n"));
    }
}