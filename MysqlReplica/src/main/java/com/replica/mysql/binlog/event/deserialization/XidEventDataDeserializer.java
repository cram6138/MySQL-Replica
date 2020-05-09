package com.replica.mysql.binlog.event.deserialization;


import java.io.IOException;

import com.replica.mysql.binlog.event.XidEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

/**
 * @author bhajuram.c
 */
public class XidEventDataDeserializer implements EventDataDeserializer<XidEventData> {

    @Override
    public XidEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        XidEventData eventData = new XidEventData();
        eventData.setXid(inputStream.readLong(8));
        return eventData;
    }
}
