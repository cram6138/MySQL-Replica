package com.replica.mysql.binlog.event;

import java.io.Serializable;

public class Event implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5387510211424603259L;
	private EventHeader header;
    private EventData data;

    public Event(EventHeader header, EventData data) {
        this.header = header;
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public <T extends EventHeader> T getHeader() {
        return (T) header;
    }

    @SuppressWarnings("unchecked")
    public <T extends EventData> T getData() {
        return (T) data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Event");
        sb.append("{header=").append(header);
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}