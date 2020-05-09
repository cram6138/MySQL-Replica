package com.replica.mysql.binlog.event;

import java.util.Arrays;

public class XAPrepareEventData implements EventData {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7573823157745329830L;
	private boolean onePhase;
	private int formatID;
	private int gtridLength;
	private int bqualLength;
	private byte[] data;
	private String gtrid;
	private String bqual;

	public boolean isOnePhase() {
		return onePhase;
	}

	public void setOnePhase(boolean onePhase) {
		this.onePhase = onePhase;
	}

	public int getFormatID() {
		return formatID;
	}

	public void setFormatID(int formatID) {
		this.formatID = formatID;
	}

	public int getGtridLength() {
		return gtridLength;
	}

	public void setGtridLength(int gtridLength) {
		this.gtridLength = gtridLength;
	}

	public int getBqualLength() {
		return bqualLength;
	}

	public void setBqualLength(int bqualLength) {
		this.bqualLength = bqualLength;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
		gtrid = new String(data, 0, gtridLength);
		bqual = new String(data, gtridLength, bqualLength);
	}

	public String getGtrid() {
		return gtrid;
	}

	public String getBqual() {
		return bqual;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("XAPrepareEventData{");
		sb.append("onePhase=").append(onePhase);
		sb.append(", formatID=").append(formatID);
		sb.append(", gtridLength=").append(gtridLength);
		sb.append(", bqualLength=").append(bqualLength);
		sb.append(", data=").append(Arrays.toString(data));
		sb.append(", gtrid='").append(gtrid).append('\'');
		sb.append(", bqual='").append(bqual).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
