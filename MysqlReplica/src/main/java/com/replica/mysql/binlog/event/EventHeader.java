package com.replica.mysql.binlog.event;

import java.io.Serializable;

public interface EventHeader extends Serializable {

	long getTimestamp();

	EventType getEventType();

	long getServerId();

	long getHeaderLength();

	long getDataLength();
}