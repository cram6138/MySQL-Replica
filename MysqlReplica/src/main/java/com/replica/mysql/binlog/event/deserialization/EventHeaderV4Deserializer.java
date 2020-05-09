package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.EventHeaderV4;
import com.replica.mysql.binlog.event.EventType;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class EventHeaderV4Deserializer implements EventHeaderDeserializer<EventHeaderV4> {

    private static final EventType[] EVENT_TYPES = EventType.values();

    @Override
    public EventHeaderV4 deserialize(ByteArrayInputStream inputStream) throws IOException {
        EventHeaderV4 header = new EventHeaderV4();
        header.setTimestamp(inputStream.readLong(4) * 1000L);
        header.setEventType(getEventType(inputStream.readInteger(1)));
        header.setServerId(inputStream.readLong(4));
        header.setEventLength(inputStream.readLong(4));
        header.setNextPosition(inputStream.readLong(4));
        header.setFlags(inputStream.readInteger(2));
        return header;
    }

    private static EventType getEventType(int ordinal) throws IOException {
        if (ordinal >= EVENT_TYPES.length) {
            throw new IOException("Unknown event type " + ordinal);
        }
        return EVENT_TYPES[ordinal];
    }

}
