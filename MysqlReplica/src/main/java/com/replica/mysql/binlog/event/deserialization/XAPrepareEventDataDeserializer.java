package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.XAPrepareEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class XAPrepareEventDataDeserializer implements EventDataDeserializer<XAPrepareEventData> {
    @Override
    public XAPrepareEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        XAPrepareEventData xaPrepareEventData = new XAPrepareEventData();
        xaPrepareEventData.setOnePhase(inputStream.read() == 0x00 ? false : true);
        xaPrepareEventData.setFormatID(inputStream.readInteger(4));
        xaPrepareEventData.setGtridLength(inputStream.readInteger(4));
        xaPrepareEventData.setBqualLength(inputStream.readInteger(4));
        xaPrepareEventData.setData(inputStream.read(
            xaPrepareEventData.getGtridLength() + xaPrepareEventData.getBqualLength()));

        return xaPrepareEventData;
    }
}
