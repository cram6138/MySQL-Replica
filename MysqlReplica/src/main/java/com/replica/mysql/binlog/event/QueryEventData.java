package com.replica.mysql.binlog.event;

public class QueryEventData implements EventData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2746353143001854295L;
	private long threadId;
	private long executionTime;
	private int errorCode;
	private String database;
	private String sql;

	public long getThreadId() {
		return threadId;
	}

	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("QueryEventData");
		sb.append("{threadId=").append(threadId);
		sb.append(", executionTime=").append(executionTime);
		sb.append(", errorCode=").append(errorCode);
		sb.append(", database='").append(database).append('\'');
		sb.append(", sql='").append(sql).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
