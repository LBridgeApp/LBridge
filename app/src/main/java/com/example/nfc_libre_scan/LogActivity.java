package com.example.nfc_libre_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.nfc_libre_scan.librelink.LibreLink;

import java.util.Arrays;

public class LogActivity extends AppCompatActivity implements View.OnClickListener, LogListener {
    private TextView logTextView;
    private int recordsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        logTextView = this.findViewById(R.id.logTextView);
        Button removeLogDbBtn = this.findViewById(R.id.removeLogsDb);
        Button clearLogWindowBtn = this.findViewById(R.id.clearLogWindowBtn);
        Button loadMoreBtn = this.findViewById(R.id.loadMore);

        removeLogDbBtn.setOnClickListener(this);
        clearLogWindowBtn.setOnClickListener(this);
        loadMoreBtn.setOnClickListener(this);

        Logger.setLoggerListener(this);

        initView();
        this.appendLogsBelow();
    }

    private void appendLogsBelow(){
        int maxId = Logger.getLogRecordCount() - 1 - recordsLoaded;
        final int LOAD_LOGS_FOR_ONE_TIME = 10;
        int minId = maxId - LOAD_LOGS_FOR_ONE_TIME + 1;
        Logger.LogRecord[] logs = Logger.getLogs(minId, maxId);
        recordsLoaded += logs.length;

        Arrays.sort(logs, (log1, log2) -> Long.compare(log2.getId(), log1.getId()));

        for(Logger.LogRecord log : logs){
            this.runOnUiThread(() -> logTextView.append(String.format("[%s] %s\n", log.getId(), log.toFullString())));
        }
        if(logs.length == 0){
            this.runOnUiThread(() -> logTextView.append("END: NO MORE LOGS.\n"));
        }
    }

    private void appendLogAbove(Logger.LogRecord log){
        StringBuilder b = new StringBuilder();
        b.append(String.format("[%s] %s\n", log.getId(), log.toFullString()));
        b.append(logTextView.getText());
        this.runOnUiThread(() -> logTextView.setText(b));
    }

    private void initView(){
        this.runOnUiThread(() -> logTextView.setText(null));
        recordsLoaded = 0;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.removeLogsDb){
            Logger.recreateLogDb();
            this.initView();
        }
        else if(v.getId() == R.id.clearLogWindowBtn){
            this.initView();
        }
        else if(v.getId() == R.id.loadMore){
            this.appendLogsBelow();
        }
    }

    @Override
    public void logReceived(Logger.LogRecord log) {
        appendLogAbove(log);
    }
}