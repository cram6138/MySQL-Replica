package com.replica.mysql.binlog.event.deserialization;

import java.io.IOException;

import com.replica.mysql.binlog.event.EventHeader;

public class EventDataDeserializationException extends IOException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 552844135854582115L;
	private EventHeader eventHeader;

    public EventDataDeserializationException(EventHeader eventHeader, Throwable cause) {
        super("Failed to deserialize data of " + eventHeader, cause);
        this.eventHeader = eventHeader;
    }

    public EventHeader getEventHeader() {
        return eventHeader;
    }
}