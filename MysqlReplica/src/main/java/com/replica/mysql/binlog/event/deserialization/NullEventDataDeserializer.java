package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.EventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class NullEventDataDeserializer implements EventDataDeserializer {

    @Override
    public EventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        return null;
    }
}
