package com.replica.mysql.binlog.event;

public class PreviousGtidSetEventData implements EventData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8390296663827478916L;
	private final String gtidSet;

	public PreviousGtidSetEventData(String gtidSet) {
		this.gtidSet = gtidSet;
	}

	public String getGtidSet() {
		return gtidSet;
	}

	@Override
	public String toString() {
		return "PreviousGtidSetEventData {gtidSet='" + gtidSet + "'}";
	}

}