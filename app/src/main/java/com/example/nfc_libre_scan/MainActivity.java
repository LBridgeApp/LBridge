package com.example.nfc_libre_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger logger = new Logger(this, findViewById(R.id.logTextView));
        LibreLinkPatcher libreLinkPatcher = new LibreLinkPatcher(this, logger);
        NfcScanner nfcScanner = new NfcScanner(this, libreLinkPatcher, logger);

        boolean nfcIsSupported = nfcScanner.nfcIsSupported();

        if (!nfcIsSupported) {
            logger.error("NFC is not supported");
            return;
        } else {
            logger.ok("NFC is supported");
        }

        AppTester appTester = new AppTester();
        boolean testsPassed = appTester.runTests();
        if (testsPassed) {
            logger.ok("Tests passed!");
        }
        if (!testsPassed) {
            logger.error("Tests failed! Exiting.");
            return;
        }

        nfcScanner.listenTags();
        libreLinkPatcher.listen();

        LibreLinkDbRemover libreLinkDbRemover = new LibreLinkDbRemover(this, logger);
        libreLinkDbRemover.listen();
    }
}