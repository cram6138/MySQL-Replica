package com.replica.mysql.binlog.event;

public class EventHeaderV4 implements EventHeader {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2348165949560945293L;
	private long timestamp;
	private EventType eventType;
	private long serverId;
	private long eventLength;
	private long nextPosition;
	private int flags;

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public EventType getEventType() {
		return eventType;
	}

	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}

	@Override
	public long getServerId() {
		return serverId;
	}

	public void setServerId(long serverId) {
		this.serverId = serverId;
	}

	public long getEventLength() {
		return eventLength;
	}

	public void setEventLength(long eventLength) {
		this.eventLength = eventLength;
	}

	public long getPosition() {
		return nextPosition - eventLength;
	}

	public long getNextPosition() {
		return nextPosition;
	}

	public void setNextPosition(long nextPosition) {
		this.nextPosition = nextPosition;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	@Override
	public long getHeaderLength() {
		return 19;
	}

	@Override
	public long getDataLength() {
		return getEventLength() - getHeaderLength();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("EventHeaderV4");
		sb.append("{timestamp=").append(timestamp);
		sb.append(", eventType=").append(eventType);
		sb.append(", serverId=").append(serverId);
		sb.append(", headerLength=").append(getHeaderLength());
		sb.append(", dataLength=").append(getDataLength());
		sb.append(", nextPosition=").append(nextPosition);
		sb.append(", flags=").append(flags);
		sb.append('}');
		return sb.toString();
	}
}
