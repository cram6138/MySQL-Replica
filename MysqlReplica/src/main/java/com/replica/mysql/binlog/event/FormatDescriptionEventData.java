package com.replica.mysql.binlog.event;

import com.replica.mysql.binlog.event.deserialization.ChecksumType;

public class FormatDescriptionEventData implements EventData {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3281925354837701878L;
	private int binlogVersion;
    private String serverVersion;
    private int headerLength;
    private int dataLength;
    private ChecksumType checksumType;

    public int getBinlogVersion() {
        return binlogVersion;
    }

    public void setBinlogVersion(int binlogVersion) {
        this.binlogVersion = binlogVersion;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public int getHeaderLength() {
        return headerLength;
    }

    public void setHeaderLength(int headerLength) {
        this.headerLength = headerLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getDataLength() {
        return dataLength;
    }

    public ChecksumType getChecksumType() {
        return checksumType;
    }

    public void setChecksumType(ChecksumType checksumType) {
        this.checksumType = checksumType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FormatDescriptionEventData");
        sb.append("{binlogVersion=").append(binlogVersion);
        sb.append(", serverVersion='").append(serverVersion).append('\'');
        sb.append(", headerLength=").append(headerLength);
        sb.append(", dataLength=").append(dataLength);
        sb.append(", checksumType=").append(checksumType);
        sb.append('}');
        return sb.toString();
    }
}
