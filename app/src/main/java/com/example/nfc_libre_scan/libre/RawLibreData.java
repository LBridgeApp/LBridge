package com.example.nfc_libre_scan.libre;

public class RawLibreData {
    private final byte[] patchUID;

    public byte[] getPatchUID() {
        return patchUID;
    }

    private final byte[] patchInfo;

    public byte[] getPatchInfo() {
        return patchInfo;
    }

    private final byte[] payload;

    public byte[] getPayload() {
        return payload;
    }

    private final long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public RawLibreData(byte[] patchUID, byte[] patchInfo, byte[] payload, long timestamp) throws Exception {
        this.patchUID = patchUID;
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.timestamp = timestamp;
        // Этот класс для gson, который не вызывает явно конструктор класса.
        // Поэтому здесь нет смысла бросать исключение.
        // Экземпляр объекта проверяется классом LibreMessage
    }

    public boolean verify() {
        return (patchUID != null
                && patchInfo != null
                && Payload.verify(payload)
                && timestamp != 0);
    }

    public static boolean verify(RawLibreData rawLibreData) {
        return rawLibreData != null && rawLibreData.verify();
    }
}
