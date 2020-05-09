package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.RotateEventData;
import com.replica.mysql.binlog.io.ByteArrayInputStream;

public class RotateEventDataDeserializer implements EventDataDeserializer<RotateEventData> {

	@Override
	public RotateEventData deserialize(ByteArrayInputStream inputStream) throws IOException {
		RotateEventData eventData = new RotateEventData();
		eventData.setBinlogPosition(inputStream.readLong(8));
		eventData.setBinlogFilename(inputStream.readString(inputStream.available()));
		return eventData;
	}
}