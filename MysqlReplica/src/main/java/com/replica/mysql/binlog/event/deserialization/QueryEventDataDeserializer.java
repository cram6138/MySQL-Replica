package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.QueryEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class QueryEventDataDeserializer implements EventDataDeserializer<QueryEventData> {

    @Override
    public QueryEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
        QueryEventData eventData = new QueryEventData();
        eventData.setThreadId(inputStream.readLong(4));
        eventData.setExecutionTime(inputStream.readLong(4));
        inputStream.skip(1); // length of the name of the database
        eventData.setErrorCode(inputStream.readInteger(2));
        inputStream.skip(inputStream.readInteger(2)); // status variables block
        eventData.setDatabase(inputStream.readZeroTerminatedString());
        eventData.setSql(inputStream.readString(inputStream.available()));
        return eventData;
    }
}
