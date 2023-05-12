package com.diabetes.lbridge.libre;

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

    public long getTimestampUTC() {
        /*
        * Несмотря на то, что UTC последнего сканирования
        * принимается от клиента, тем не менее, разница во времени
        * между клиентом и сервером неопасна, так как на сервере
        * проводится валидация времени.
        * То есть, будет пресечена запись в таблицу LibreLink,
        * если последний UTC timestamp в ней больше, чем
        * текущий timestamp от клиента.*/
        return timestamp;
    }

    public RawLibreData(byte[] patchUID, byte[] patchInfo, byte[] payload, long timestampUTC) throws Exception {
        this.patchUID = patchUID;
        this.patchInfo = patchInfo;
        this.payload = payload;
        this.timestamp = timestampUTC;
        // Этот класс для gson, который не вызывает явно конструктор класса.
        // Поэтому здесь нет смысла бросать исключение.
        // Экземпляр объекта проверяется классом LibreMessage
    }

    public void validate() throws Exception {
        boolean patchUidValid = patchUID != null && patchUID.length == 8;
        boolean patchInfoValid = patchInfo != null && patchInfo.length == 6;
        boolean payloadValid = payload != null && Payload.verify(payload);
        boolean timestampUtcValid = timestamp > 0;

        if(!patchUidValid){
            throw new Exception(String.format("PatchUID is not valid.\n" +
                    "His length: %s",
                    (patchUID == null) ? 0 : patchUID.length));
        }

        if(!patchInfoValid){
            throw new Exception(String.format("PatchInfo is not valid.\n" +
                            "His length: %s",
                    (patchInfo == null) ? 0 : patchInfo.length));
        }

        if(!payloadValid){
            throw new Exception(String.format("Payload is not valid.\n" +
                            "His length: %s",
                    (payload == null) ? 0 : payload.length));
        }

        if(!timestampUtcValid){
            throw new Exception(String.format("Timestamp is not valid.\n" +
                            "His value: %s", timestamp));
        }
    }

    /*public static boolean validate(RawLibreData rawLibreData) {
        return rawLibreData != null && rawLibreData.validate();
    }*/
}
