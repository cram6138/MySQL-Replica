package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.EventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public interface EventDataDeserializer<T extends EventData> {

	T deserialize(ByteArrayInputStream inputStream) throws IOException;
}