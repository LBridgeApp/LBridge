package com.diabetes.lbridge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CriticalErrorActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_critical_error);

        Intent intent = getIntent();
        Throwable errorObject = (Throwable) intent.getSerializableExtra("criticalError");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        errorObject.printStackTrace(pw);
        String errorMessage = sw.toString();

        TextView errorTextView = findViewById(R.id.criticalErrorTextView);
        this.runOnUiThread(() -> errorTextView.setText(errorMessage));

        Button restartButton = this.findViewById(R.id.restartAppButton);
        restartButton.setOnClickListener(this);

        Vibrator.CRITICAL_ERROR.vibrate(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.restartAppButton) {
            restartApp();
        }
    }

    private void restartApp() {
        Intent intent = this.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.startActivity(intent);
        }
        Runtime.getRuntime().exit(0);
    }
}