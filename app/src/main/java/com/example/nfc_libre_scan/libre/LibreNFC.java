package com.example.nfc_libre_scan.libre;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcV;
import android.os.Bundle;

import com.example.nfc_libre_scan.Logger;
import com.example.nfc_libre_scan.OnLibreMessageListener;
import com.example.nfc_libre_scan.Vibrator;
import com.oop1.AlgorithmRunner;
import com.oop1.OOPResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LibreNFC implements NfcAdapter.ReaderCallback {
    private final Activity activity;
    private NfcV nfcVTag;
    private final List<OnLibreMessageListener> listeners = new ArrayList<>();

    public LibreNFC(Activity activity) {
        this.activity = activity;
    }

    public void listenSensor() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        nfcAdapter.disableReaderMode(activity);
        Logger.ok("NFC reader mode disabled");
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
        Logger.ok("NFC reader mode enabled");
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        Logger.inf("\n----------------\n");
        Logger.ok("NfcV tag discovered");
        Vibrator.SCAN_START.vibrate(activity);
        try (NfcV nfcVTag = NfcV.get(tag)) {
            this.nfcVTag = nfcVTag;
            if (!nfcVTag.isConnected()) {
                nfcVTag.connect();
                Logger.ok("NfcV tag connected");
            }

            byte[] patchUID = this.queryPatchUID();
            byte[] patchInfo = this.queryPatchInfo();
            byte[] payload = this.queryPayload();
            long timestamp = System.currentTimeMillis();
            RawLibreData rawLibreData = new RawLibreData(patchUID, patchInfo, payload, timestamp);
            LibreMessage libreMessage = LibreMessage.getInstance(activity, rawLibreData);
            listeners.forEach(l -> l.onLibreMessageReceived(libreMessage));
            Vibrator.SCAN_SUCCESS.vibrate(activity);

        } catch (Exception e) {
            Logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
            Vibrator.SCAN_ERROR.vibrate(activity);
        } finally {
            Logger.ok("NfcV tag closed");
        }
    }

    public void setLibreListener(OnLibreMessageListener listener) {
        this.listeners.add(listener);
    }
    protected byte[] runCmd(byte[] cmd) throws IOException, InterruptedException {
        long time = System.currentTimeMillis();
        byte[] response = null;
        boolean continueOperation = true;
        do {
            try {
                response = nfcVTag.transceive(cmd);
                continueOperation = false;
            } catch (TagLostException ignored) {
                if (System.currentTimeMillis() > time + 2000) {
                    throw new IOException("More than 2 seconds tag lost.");
                } else {
                    Thread.sleep(100);
                }
            }
        }
        while (continueOperation);
        return response;
    }

    private byte[] queryPatchUID() {
        return nfcVTag.getTag().getId();
    }

    private byte[] queryPatchInfo() throws Exception {
        //Logger.inf("Getting patchInfo...");
        // 0x02 - код команды Read Single Block, используемый для чтения одного блока данных с NFC-тега.
        // (byte) 0xa1 - код блока, который нужно прочитать.
        // 0x07 - номер блока, с которого нужно начать чтение.
        final byte[] cmd = new byte[]{0x02, (byte) 0xa1, 0x07};
        byte[] patchInfo = this.runCmd(cmd);
        // Нужно отбросить первый нулевой байт
        patchInfo = Arrays.copyOfRange(patchInfo, 1, patchInfo.length);
        //Logger.ok("PatchInfo retrieved.");
        if(patchInfo.length != 6){ throw new Exception("PatchInfo is not valid."); }
        return patchInfo;
    }

    private byte[] queryPayload() throws Exception {
        byte[] payload = new byte[1000];
        //Logger.inf("Getting payload...");
        final int correct_reply_size = 9;
        final int startBlock = 1;
        for (int i = 0; i < 43; i++) {
            final byte[] cmd = new byte[]{(byte) 0x02, (byte) 0x23, (byte) i, (byte) 0x0};
            byte[] oneBlock;
            do {
                oneBlock = this.runCmd(cmd);
            }
            while (oneBlock.length != correct_reply_size);
            System.arraycopy(oneBlock, startBlock, payload, i * 8, 8);
        }
        //Logger.ok("Payload retrieved.");
        payload = Arrays.copyOfRange(payload, 0, Payload.payloadBytesLength);

        boolean payloadIsValid = Payload.verify(payload);
        if(!payloadIsValid){
            throw new Exception("Payload is not valid.");
        }
        return payload;
    }
}
