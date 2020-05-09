package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.EventHeader;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public interface EventHeaderDeserializer<T extends EventHeader> {

    T deserialize(ByteArrayInputStream inputStream) throws IOException;
}