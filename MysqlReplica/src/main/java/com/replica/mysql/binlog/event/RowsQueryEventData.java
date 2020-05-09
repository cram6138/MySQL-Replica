package com.replica.mysql.binlog.event;

public class RowsQueryEventData implements EventData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2237595146552152627L;
	private String query;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("RowsQueryEventData");
		sb.append("{query='").append(query).append('\'');
		sb.append('}');
		return sb.toString();
	}

}