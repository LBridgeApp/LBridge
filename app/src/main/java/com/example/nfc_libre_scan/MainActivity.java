package com.example.nfc_libre_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
    private GlucoseUnit glucoseUnit = GlucoseUnit.MMOL;

    // TODO: Интерфейс активити: сделать количество минут рандомизации отправки сервиса.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new CriticalErrorHandler().setHandler();
        setContentView(R.layout.activity_main);

        logTextView = this.findViewById(R.id.logTextView);
        Logger.setLoggerListener(this);

        LibreNFC libreNFC = new LibreNFC(this);
        libreNFC.listenSensor();
        libreNFC.setLibreMessageListener(this);

        currentBgView = this.findViewById(R.id.currentBgView);
        bgHistoryView = this.findViewById(R.id.bgHistoryView);

        Button sugarAddingBtn = this.findViewById(R.id.sugarAddingBtn);
        Button databaseRemovingBtn = this.findViewById(R.id.removeLibrelinkDB);

        Button loadLogFromDbBtn = this.findViewById(R.id.loadLogsFromDB);
        Button removeLogDbBtn = this.findViewById(R.id.removeLogsDb);
        Button clearLogWindowBtn = this.findViewById(R.id.clearLogWindow);

        loadLogFromDbBtn.setOnClickListener(this);
        removeLogDbBtn.setOnClickListener(this);
        clearLogWindowBtn.setOnClickListener(this);

        libreLink = new LibreLink(this);
        libreLink.listenLibreMessages(libreNFC);
        sugarAddingBtn.setOnClickListener(this);
        databaseRemovingBtn.setOnClickListener(this);


        if (glucoseUnit == GlucoseUnit.MMOL) {
            ((RadioButton) this.findViewById(R.id.mmolGlucoseUnit)).setChecked(true);
        }
        if (glucoseUnit == GlucoseUnit.MGDL) {
            ((RadioButton) this.findViewById(R.id.mgdlGlucoseUnit)).setChecked(true);
        }

        RadioGroup glucoseUnitRadioGroup = this.findViewById(R.id.glucose_unit);
        glucoseUnitRadioGroup.setOnCheckedChangeListener(this);

        WebService.startService(this);

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
        this.runOnUiThread(() -> logTextView.append("\n" + log.toShortString() + "\n"));
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.loadLogsFromDB){
            Logger.LogRecord[] logs = Logger.getLogs();
            this.runOnUiThread(() -> logTextView.setText(null));
            for(Logger.LogRecord log : logs){
                this.runOnUiThread(() -> logTextView.append("\n" + log.toShortString() + "\n"));
            }
        }
        else if(v.getId() == R.id.removeLogsDb){
            Logger.recreateLogDb();
            this.runOnUiThread(() -> logTextView.setText(null));
        }
        else if(v.getId() == R.id.clearLogWindow){
            this.runOnUiThread(() -> logTextView.setText(null));
        }
        else if (v.getId() == R.id.sugarAddingBtn) {
            try {
                libreLink.addLastScanToDatabase();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        else if (v.getId() == R.id.removeLibrelinkDB) {
            try {
                libreLink.removeLibreLinkDatabases();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }
}