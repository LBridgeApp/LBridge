package com.example.nfc_libre_scan.libre;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.NfcV;
import android.os.Bundle;

import com.example.nfc_libre_scan.Logger;
import com.example.nfc_libre_scan.OnLibreMessageListener;
import com.example.nfc_libre_scan.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Libre implements NfcAdapter.ReaderCallback {
    private final Activity activity;
    private final Logger logger;
    private NfcV nfcVTag;
    private final List<OnLibreMessageListener> listeners = new ArrayList<>();

    public Libre(Activity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
    }

    public void listenSensor() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
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

    @Override
    public void onTagDiscovered(Tag tag) {
        logger.inf("\n----------------\n");
        logger.ok("NfcV tag discovered");
        try (NfcV nfcVTag = NfcV.get(tag)) {
            this.nfcVTag = nfcVTag;
            if (!nfcVTag.isConnected()) {
                nfcVTag.connect();
                logger.ok("NfcV tag connected");
            }

            PatchUID patchUID = this.queryPatchUID();
            byte[] patchInfo = this.queryPatchInfo();
            Payload payload = this.queryPayload();
            String libreSN = patchUID.decodeSerialNumber();

            /*
            byte[] patchInfo = Utils.convertByteStringToByteArray("a2 08 00 08 06 20");
            Payload payload = new Payload(Utils.convertByteStringToByteArray("2d 2f c8 1b 03 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 c7 df 0d 00 9b 05 c8 90 1b 80 b0 05 c8 84 1b 80 bd 05 c8 74 1b 80 d0 05 c8 60 1b 80 da 05 c8 54 1b 80 e7 05 c8 54 1b 80 f8 05 c8 58 1b 80 09 06 c8 64 1b 80 1e 06 c8 5c 1b 80 3d 06 c8 6c 1b 80 4c 06 c8 74 1b 80 60 06 c8 54 1b 80 76 06 c8 50 1b 80 58 05 88 72 5b 80 70 05 c8 5c 1b 80 7d 05 c8 88 1b 80 3c 05 c8 4c 5f 80 26 05 c8 ac 5e 80 33 05 c8 a0 1d 80 8f 05 c8 2c 1d 80 07 06 c8 9c 5d 80 1f 06 c8 fc 1d 80 02 06 c8 58 5e 80 83 05 c8 08 5e 80 6b 04 c8 e8 5d 80 b9 03 c8 60 1d 80 36 03 c8 c0 5d 80 17 03 c8 50 5c 80 31 02 c8 84 1a 80 85 01 c8 c0 1a 80 74 01 c8 1c 1b 80 c3 01 c8 2c 1b 80 94 02 88 36 9b 80 14 03 c8 84 5b 80 43 03 c8 1c 1b 80 7f 03 c8 04 1b 80 d8 03 c8 5c 1b 80 a4 03 c8 a8 1b 80 f7 02 c8 f4 1b 80 6f 02 c8 d4 1b 80 37 02 c8 10 1c 80 3d 02 c8 fc 1b 80 87 02 c8 c4 1c 80 db 02 c8 b0 1c 80 3d 03 c8 f0 1b 80 bf 03 c8 bc 1b 80 84 04 c8 d0 1b 80 8e 05 c8 88 1b 80 ee 01 00 00 4d a7 00 08 df 0d 63 51 14 07 96 80 5a 00 ed a6 08 84 1a c8 04 34 78 65"));
            */
            
            LibreMessage libreMessage = new LibreMessage(patchUID, patchInfo, payload, libreSN, activity);
            listeners.forEach(l -> l.onLibreMessageReceived(libreMessage));

        } catch (Exception e) {
            logger.error(Objects.requireNonNull(e.getLocalizedMessage()));
        } finally {
            logger.ok("NfcV tag closed");
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

    private PatchUID queryPatchUID() throws Exception {
        return new PatchUID(nfcVTag.getTag().getId());
    }

    private byte[] queryPatchInfo() throws Exception {
        //logger.inf("Getting patchInfo...");
        // 0x02 - код команды Read Single Block, используемый для чтения одного блока данных с NFC-тега.
        // (byte) 0xa1 - код блока, который нужно прочитать.
        // 0x07 - номер блока, с которого нужно начать чтение.
        final byte[] cmd = new byte[]{0x02, (byte) 0xa1, 0x07};
        byte[] patchInfo = this.runCmd(cmd);
        // Нужно отбросить первый нулевой байт
        patchInfo = Arrays.copyOfRange(patchInfo, 1, patchInfo.length);
        //logger.ok("PatchInfo retrieved.");
        if(patchInfo.length != 6){ throw new Exception("PatchInfo is not valid."); }
        return patchInfo;
    }

    private Payload queryPayload() throws Exception {
        byte[] data = new byte[1000];
        //logger.inf("Getting payload...");
        final int correct_reply_size = 9;
        final int startBlock = 1;
        for (int i = 0; i < 43; i++) {
            final byte[] cmd = new byte[]{(byte) 0x02, (byte) 0x23, (byte) i, (byte) 0x0};
            byte[] oneBlock;
            do {
                oneBlock = this.runCmd(cmd);
            }
            while (oneBlock.length != correct_reply_size);
            System.arraycopy(oneBlock, startBlock, data, i * 8, 8);
        }
        //logger.ok("Payload retrieved.");
        data = Arrays.copyOfRange(data, 0, Payload.payloadBytesLength);
        return new Payload(data);
    }
}
