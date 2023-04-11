package com.example.nfc_libre_scan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;

import com.hg4.oopalgorithm.oopalgorithm.CurrentBg;
import com.hg4.oopalgorithm.oopalgorithm.HistoricBg;

import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements OnLibreMessageListener {
    TextView currentBgView;
    TextView bgHistoryView;
    Logger logger;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentBgView = this.findViewById(R.id.currentBgView);
        bgHistoryView = this.findViewById(R.id.bgHistoryView);
        vibrator = (Vibrator) ContextCompat.getSystemService(this, Vibrator.class);
        logger = new Logger(this, findViewById(R.id.logTextView));
        LibreLink libreLink = new LibreLink(this, logger);
        NfcScanner nfcScanner = new NfcScanner(this, logger);

        boolean nfcIsSupported = nfcScanner.nfcIsSupported();

        if (!nfcIsSupported) {
            logger.error("NFC is not supported");
            return;
        } else {
            logger.ok("NFC is supported");
        }

        AppTester appTester = new AppTester(this, logger);
        boolean testsPassed = appTester.runTests();
        if (testsPassed) {
            logger.ok("Tests passed!");
        }
        if (!testsPassed) {
            logger.error("Tests failed! Exiting.");
            return;
        }

        nfcScanner.listenTags();

        Button sugarAddingBtn = this.findViewById(R.id.sugarAddingBtn);
        Button databaseRemovingBtn = this.findViewById(R.id.removeLibrelinkDB);

        sugarAddingBtn.setOnClickListener(libreLink);
        databaseRemovingBtn.setOnClickListener(libreLink);

        libreLink.listenLibreMessages(nfcScanner);
        nfcScanner.setOnLibreMessageListener(this);
    }

    private void vibrate() {
        VibrationEffect vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE);
        vibrator.vibrate(vibrationEffect);
    }

    @Override
    public void onLibreMessageReceived(LibreMessage message) {
        CurrentBg currentBgObject;
        HistoricBg[] historicBgArray;
        try {
            currentBgObject = message.getCurrentBgObject().convertBG(LibreMessage.GlucoseUnit.MMOL);
            double currentBG = currentBgObject.getBG();
            String glucoseUnit = currentBgObject.getGlucoseUnit().getString();
            historicBgArray = message.getHistoricBgArray();


            String textString = String.format(Locale.US, "Current BG: %.2f %s\n", currentBG, glucoseUnit);
            this.runOnUiThread(() -> this.currentBgView.setText(textString));

            StringBuilder historicBgBuilder = new StringBuilder();
            for (HistoricBg historicBg : historicBgArray) {
                historicBg = historicBg.convertBG(LibreMessage.GlucoseUnit.MMOL);
                double bg = historicBg.getBG();
                glucoseUnit = historicBg.getGlucoseUnit().getString();
                long time = historicBg.getTime();

                final String str = String.format(Locale.US, "Time: %d, BG: %.2f %s\n", time, bg, glucoseUnit);
                historicBgBuilder.append(str);
            }
            this.runOnUiThread(() -> this.bgHistoryView.setText(historicBgBuilder));

        } catch (Exception e) {
            logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
        }
        this.vibrate();
    }
}