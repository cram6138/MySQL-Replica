package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.RowsQueryEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class RowsQueryEventDataDeserializer implements EventDataDeserializer<RowsQueryEventData> {

    @Override
    public RowsQueryEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        RowsQueryEventData eventData = new RowsQueryEventData();
        inputStream.readInteger(1); // ignored
        eventData.setQuery(inputStream.readString(inputStream.available()));
        return eventData;
    }

}
