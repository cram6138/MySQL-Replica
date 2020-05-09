package com.replica.mysql.binlog.event;

public class IntVarEventData implements EventData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1734724298214204371L;
	/**
	 * Type indicating whether the value is meant to be used for the
	 * LAST_INSERT_ID() invocation (should be equal 1) or AUTO_INCREMENT column
	 * (should be equal 2).
	 */
	private int type;
	private long value;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("IntVarEventData");
		sb.append("{type=").append(type);
		sb.append(", value=").append(value);
		sb.append('}');
		return sb.toString();
	}

}
