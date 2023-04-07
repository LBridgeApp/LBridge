package com.example.nfc_libre_scan;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;

import java.util.Objects;

public class NfcScanner implements NfcAdapter.ReaderCallback {
    private final NfcAdapter nfcAdapter;
    private final Activity activity;
    private final Logger logger;
    private final LibreLink libreLink;

    NfcScanner(Activity activity, LibreLink libreLink, Logger logger) {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        this.activity = activity;
        this.libreLink = libreLink;
        this.logger = logger;
    }

    void listenTags() {
        nfcAdapter.disableReaderMode(activity);
        logger.ok("NFC reader mode disabled");
        /*
         * "FLAG_READER_NFC_V" - указывает, что считыватель должен быть настроен для чтения технологии NFC-V (ISO15693).
         *  "FLAG_READER_SKIP_NDEF_CHECK" - указывает, что при чтении тега не нужно проверять наличие NDEF сообщения на теге.
         * "FLAG_READER_NO_PLATFORM_SOUNDS" - указывает, что при взаимодействии с тегом не нужно проигрывать звуковые эффекты, которые предусмотрены платформой Android.
         * options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000) увеличено время ожидания NFC-тега до 5_000 мс
         */
        int flags = NfcAdapter.FLAG_READER_NFC_V | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;

        final Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);

        nfcAdapter.enableReaderMode(activity, this, flags, options);
        logger.ok("NFC reader mode enabled");
    }

    boolean nfcIsSupported() {
        return nfcAdapter != null;
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        logger.inf("\n----------------\n");
        logger.ok("NfcV tag discovered");
        try (NfcV nfcVTag = NfcV.get(tag)) {
            LibreMessage libreMessage = new LibreMessage(nfcVTag, logger);
            if (!nfcVTag.isConnected()) {
                nfcVTag.connect();
                logger.ok("NfcV tag connected");
            }

            libreMessage.handle();
            libreLink.onLibreMessageReceived(libreMessage);
        } catch (Exception e) {
            logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
        } finally {
            logger.ok("NfcV tag closed");
        }
    }
}
