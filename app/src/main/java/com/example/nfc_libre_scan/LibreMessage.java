package com.example.nfc_libre_scan;

import android.content.Context;
import android.nfc.TagLostException;
import android.nfc.tech.NfcV;

import com.hg4.oopalgorithm.oopalgorithm.AlgorithmRunner;
import com.hg4.oopalgorithm.oopalgorithm.CurrentBg;
import com.hg4.oopalgorithm.oopalgorithm.HistoricBg;
import com.hg4.oopalgorithm.oopalgorithm.OOPResults;

import java.io.IOException;
import java.util.Arrays;

public class LibreMessage {
    public enum GlucoseUnit {
        MGDL("mg/dL"),
        MMOL("mmol/L");

        private final String unitText;
        GlucoseUnit(String unitText){
            this.unitText = unitText;
        }

        public String getString() {
            return this.unitText;
        }
    }

    private final Context context;
    private final Logger logger;
    private final NfcV nfcVTag;
    private byte[] patchUID;
    private byte[] patchInfo;
    private String libreSN;

    public byte[] getPatchInfo() {
        return this.patchInfo;
    }

    private byte[] payload;

    public byte[] getPayload() {
        return this.payload;
    }

    private OOPResults oopResults;

    private OOPResults getOopResults() {
        return oopResults;
    }

    public CurrentBg getCurrentBgObject() {
        return getOopResults().getCurrentBgObject();
    }

    public HistoricBg[] getHistoricBgArray() {
        return getOopResults().getHistoricBgArray();
    }

    LibreMessage(Context context, NfcV nfcVTag, Logger logger) {
        this.context = context;
        this.logger = logger;
        this.nfcVTag = nfcVTag;
    }

    public void handle() throws Exception {
        logger.inf("Handling libre message...");

        patchUID = queryPatchUID();
        patchInfo = queryPatchInfo();
        payload = queryPayload();
        libreSN = decodeSerialNumber();
        oopResults = queryOOPResults();
        int l = 5;
    }

    private byte[] queryPatchInfo() throws IOException, InterruptedException {
        logger.inf("Getting patchInfo...");
        // 0x02 - код команды Read Single Block, используемый для чтения одного блока данных с NFC-тега.
        // (byte) 0xa1 - код блока, который нужно прочитать.
        // 0x07 - номер блока, с которого нужно начать чтение.
        final byte[] cmd = new byte[]{0x02, (byte) 0xa1, 0x07};
        byte[] patchInfo = runCmd(cmd);
        // Нужно отбросить первый нулевой байт
        patchInfo = Arrays.copyOfRange(patchInfo, 1, patchInfo.length);
        logger.ok("PatchInfo retrieved.");
        return patchInfo;
    }

    private byte[] runCmd(byte[] cmd) throws IOException, InterruptedException {
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

    private byte[] queryPayload() throws IOException, InterruptedException {
        byte[] data = new byte[1000];
        logger.inf("Getting payload...");
        final int correct_reply_size = 9;
        final int startBlock = 1;
        for (int i = 0; i < 43; i++) {
            final byte[] cmd = new byte[]{(byte) 0x02, (byte) 0x23, (byte) i, (byte) 0x0};
            byte[] oneBlock;
            do {
                oneBlock = runCmd(cmd);
            }
            while (oneBlock.length != correct_reply_size);
            System.arraycopy(oneBlock, startBlock, data, i * 8, 8);
        }
        logger.ok("Payload retrieved.");

        boolean payloadIsCorrect = verifyPayload(data);
        if (payloadIsCorrect) {
            logger.ok("Payload is correct.");
        } else {
            throw new IOException("Payload is not correct.");
        }

        // if data length is more than 344 return first 344 bytes
        return Arrays.copyOfRange(data, 0, LIBRE_1_2_FRAM_SIZE);
    }

    private byte[] queryUID() {
        return nfcVTag.getTag().getId();
    }

    private byte[] queryPatchUID() {
        return queryUID();
    }

    private OOPResults queryOOPResults() {
        long timestamp = System.currentTimeMillis();
        return AlgorithmRunner.RunAlgorithm(timestamp, context, payload, patchUID, patchInfo, false, libreSN);
    }

    public String decodeSerialNumber() {
        byte[] serialBuffer = new byte[3 + 8];
        System.arraycopy(patchUID, 0, serialBuffer, 3, 8);

        String[] lookupTable =
                {
                        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                        "A", "C", "D", "E", "F", "G", "H", "J", "K", "L",
                        "M", "N", "P", "Q", "R", "T", "U", "V", "W", "X",
                        "Y", "Z"
                };
        byte[] uuidShort = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        int i;

        for (i = 2; i < 8; i++) uuidShort[i - 2] = serialBuffer[(2 + 8) - i];
        uuidShort[6] = 0x00;
        uuidShort[7] = 0x00;

        StringBuilder binary = new StringBuilder();
        String binS;
        for (i = 0; i < 8; i++) {
            binS = String.format("%8s", Integer.toBinaryString(uuidShort[i] & 0xFF)).replace(' ', '0');
            binary.append(binS);
        }

        StringBuilder v = new StringBuilder("0");
        char[] pozS = {0, 0, 0, 0, 0};
        for (i = 0; i < 10; i++) {
            for (int k = 0; k < 5; k++) pozS[k] = binary.charAt((5 * i) + k);
            int value = (pozS[0] - '0') * 16 + (pozS[1] - '0') * 8 + (pozS[2] - '0') * 4 + (pozS[3] - '0') * 2 + (pozS[4] - '0') * 1;
            v.append(lookupTable[value]);
        }

        return v.toString();
    }

    public boolean verifyPayload(byte[] data) {
        // Continue for libre1,2 checks
        if (data.length < LIBRE_1_2_FRAM_SIZE) {
            logger.error("Must have at least 344 bytes for libre data");
            return false;
        }
        // Проверка диапазона 0 - 343 байтов одной операцией не работает.
        boolean checksum_ok = checkCRC16(data, 0, 24); // 0th - 23th bytes
        checksum_ok &= checkCRC16(data, 24, 296); // 24th - 319th bytes
        checksum_ok &= checkCRC16(data, 320, 24); // 320th - 343th bytes
        return checksum_ok;

    }

    boolean checkCRC16(byte[] data, int start, int size) {
        long crc = computeCRC16(data, start, size);
        return crc == ((data[start + 1] & 0xFF) * 256 + (data[start] & 0xff));
    }

    long computeCRC16(byte[] data, int start, int size) {
        long crc = 0xffff;
        for (int i = start + 2; i < start + size; i++) {
            crc = ((crc >> 8) ^ crc16table[(int) (crc ^ (data[i] & 0xFF)) & 0xff]);
        }

        long reverseCrc = 0;
        for (int i = 0; i < 16; i++) {
            reverseCrc = (reverseCrc << 1) | (crc & 1);
            crc >>= 1;
        }
        return reverseCrc;
    }

    // Constants for libre1/2 FRAM
    private final int FRAM_RECORD_SIZE = 6;
    private final int TREND_START = 28;
    private final int HISTORY_START = 124;
    private final int LIBRE_1_2_FRAM_SIZE = 344;

    private final long[] crc16table = {
            0, 4489, 8978, 12955, 17956, 22445, 25910, 29887, 35912,
            40385, 44890, 48851, 51820, 56293, 59774, 63735, 4225, 264,
            13203, 8730, 22181, 18220, 30135, 25662, 40137, 36160, 49115,
            44626, 56045, 52068, 63999, 59510, 8450, 12427, 528, 5017,
            26406, 30383, 17460, 21949, 44362, 48323, 36440, 40913, 60270,
            64231, 51324, 55797, 12675, 8202, 4753, 792, 30631, 26158,
            21685, 17724, 48587, 44098, 40665, 36688, 64495, 60006, 55549,
            51572, 16900, 21389, 24854, 28831, 1056, 5545, 10034, 14011,
            52812, 57285, 60766, 64727, 34920, 39393, 43898, 47859, 21125,
            17164, 29079, 24606, 5281, 1320, 14259, 9786, 57037, 53060,
            64991, 60502, 39145, 35168, 48123, 43634, 25350, 29327, 16404,
            20893, 9506, 13483, 1584, 6073, 61262, 65223, 52316, 56789,
            43370, 47331, 35448, 39921, 29575, 25102, 20629, 16668, 13731,
            9258, 5809, 1848, 65487, 60998, 56541, 52564, 47595, 43106,
            39673, 35696, 33800, 38273, 42778, 46739, 49708, 54181, 57662,
            61623, 2112, 6601, 11090, 15067, 20068, 24557, 28022, 31999,
            38025, 34048, 47003, 42514, 53933, 49956, 61887, 57398, 6337,
            2376, 15315, 10842, 24293, 20332, 32247, 27774, 42250, 46211,
            34328, 38801, 58158, 62119, 49212, 53685, 10562, 14539, 2640,
            7129, 28518, 32495, 19572, 24061, 46475, 41986, 38553, 34576,
            62383, 57894, 53437, 49460, 14787, 10314, 6865, 2904, 32743,
            28270, 23797, 19836, 50700, 55173, 58654, 62615, 32808, 37281,
            41786, 45747, 19012, 23501, 26966, 30943, 3168, 7657, 12146,
            16123, 54925, 50948, 62879, 58390, 37033, 33056, 46011, 41522,
            23237, 19276, 31191, 26718, 7393, 3432, 16371, 11898, 59150,
            63111, 50204, 54677, 41258, 45219, 33336, 37809, 27462, 31439,
            18516, 23005, 11618, 15595, 3696, 8185, 63375, 58886, 54429,
            50452, 45483, 40994, 37561, 33584, 31687, 27214, 22741, 18780,
            15843, 11370, 7921, 3960};
}
