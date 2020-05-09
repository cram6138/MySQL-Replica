package com.replica.mysql.binlog.event;

public class XidEventData implements EventData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7812507533065911044L;
	private long xid;

	public long getXid() {
		return xid;
	}

	public void setXid(long xid) {
		this.xid = xid;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("XidEventData");
		sb.append("{xid=").append(xid);
		sb.append('}');
		return sb.toString();
	}
}
