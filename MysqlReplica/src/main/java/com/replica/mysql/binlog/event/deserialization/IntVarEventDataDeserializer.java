package com.replica.mysql.binlog.event.deserialization;


import java.io.IOException;

import com.replica.mysql.binlog.event.IntVarEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

/**
 * @author bhajuram.c
 */
public class IntVarEventDataDeserializer implements EventDataDeserializer<IntVarEventData> {

    @Override
    public IntVarEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        IntVarEventData event = new IntVarEventData();
        event.setType(inputStream.readInteger(1));
        event.setValue(inputStream.readLong(8));
        return event;
    }
}
