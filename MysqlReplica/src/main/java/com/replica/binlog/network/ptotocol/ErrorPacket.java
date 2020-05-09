package com.replica.binlog.network.ptotocol;

import java.io.IOException;

import com.replica.mysql.binlog.io.ByteArrayInputStream;
import com.replica.mysql.binlog.network.Packet;

public class ErrorPacket implements Packet {

    private int errorCode;
    private String sqlState;
    private String errorMessage;

    public ErrorPacket(byte[] bytes) throws IOException {
        ByteArrayInputStream buffer = new ByteArrayInputStream(bytes);
        this.errorCode = buffer.readInteger(2);
        if (buffer.peek() == '#') {
            buffer.skip(1); // marker of the SQL State
            this.sqlState = buffer.readString(5);
        }
        this.errorMessage = buffer.readString(buffer.available());
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
