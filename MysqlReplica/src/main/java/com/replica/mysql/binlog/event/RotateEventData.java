package com.replica.mysql.binlog.event;

public class RotateEventData implements EventData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4122006112123910872L;
	private String binlogFilename;
	private long binlogPosition;

	public String getBinlogFilename() {
		return binlogFilename;
	}

	public void setBinlogFilename(String binlogFilename) {
		this.binlogFilename = binlogFilename;
	}

	public long getBinlogPosition() {
		return binlogPosition;
	}

	public void setBinlogPosition(long binlogPosition) {
		this.binlogPosition = binlogPosition;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("RotateEventData");
		sb.append("{binlogFilename='").append(binlogFilename).append('\'');
		sb.append(", binlogPosition=").append(binlogPosition);
		sb.append('}');
		return sb.toString();
	}
}
