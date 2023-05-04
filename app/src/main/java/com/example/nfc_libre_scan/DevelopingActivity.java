package com.example.nfc_libre_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.nfc_libre_scan.librelink.LibreLink;

public class DevelopingActivity extends AppCompatActivity implements View.OnClickListener, LogListener {
    private TextView logTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developing);
        setTitle(String.format("%s %s", this.getString(R.string.app_name), "Developing options"));

        logTextView = this.findViewById(R.id.logTextView);
        Logger.setLoggerListener(this);

        Button databaseRemovingBtn = this.findViewById(R.id.removeLibrelinkDB);
        Button fakeSerialNumberBtn = findViewById(R.id.fake_serial_button);
        Button endCurrentSensorBtn = findViewById(R.id.end_current_sensor_btn);
        Button clearLogBtn = findViewById(R.id.clearLogWindowBtn);

        clearLogBtn.setOnClickListener(this);
        fakeSerialNumberBtn.setOnClickListener(this);
        endCurrentSensorBtn.setOnClickListener(this);
        databaseRemovingBtn.setOnClickListener(this);
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
        if(v.getId() == R.id.clearLogWindowBtn){
            this.runOnUiThread(() -> logTextView.setText(null));
        }
        else if(v.getId() == R.id.fake_serial_button){
            try {
                setFakeSerialNumberForLastSensor();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        else if(v.getId() == R.id.end_current_sensor_btn){
            try {
                endCurrentSensor();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        else if(v.getId() == R.id.removeLibrelinkDB){
            try {
                LibreLink libreLink = new LibreLink(this);
                libreLink.removeLibreLinkDatabases();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    public void setFakeSerialNumberForLastSensor() throws Exception {
        LibreLink libreLink = new LibreLink(this);
        libreLink.setFakeSerialNumberForLastSensor();
    }

    public void endCurrentSensor() throws Exception {
        LibreLink libreLink = new LibreLink(this);
        libreLink.endCurrentSensor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_developing_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.open_logs) {
            Intent intent = new Intent(this, LogActivity.class);
            this.startActivity(intent);
        } else if (item.getItemId() == R.id.exit) {
            this.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}