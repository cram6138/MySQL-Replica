package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.GtidEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class GtidEventDataDeserializer implements EventDataDeserializer<GtidEventData> {

    @Override
    public GtidEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        GtidEventData eventData = new GtidEventData();
        byte flags = (byte) inputStream.readInteger(1);
        byte[] sid = inputStream.read(16);
        long gno = inputStream.readLong(8);
        eventData.setFlags(flags);
        eventData.setGtid(byteArrayToHex(sid, 0, 4) + "-" +
            byteArrayToHex(sid, 4, 2) + "-" +
            byteArrayToHex(sid, 6, 2) + "-" +
            byteArrayToHex(sid, 8, 2) + "-" +
            byteArrayToHex(sid, 10, 6) + ":" +
            String.format("%d", gno)
        );
        return eventData;
    }

    private String byteArrayToHex(byte[] a, int offset, int len) {
        StringBuilder sb = new StringBuilder();
        for (int idx = offset; idx < (offset + len) && idx < a.length; idx++) {
            sb.append(String.format("%02x", a[idx] & 0xff));
        }
        return sb.toString();
    }

}
