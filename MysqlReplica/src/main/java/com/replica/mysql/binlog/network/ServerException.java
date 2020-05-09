package com.replica.mysql.binlog.network;

import java.io.IOException;

public class ServerException extends IOException {

    private int errorCode;
    private String sqlState;

    public ServerException(String message, int errorCode, String sqlState) {
        super(message);
        this.errorCode = errorCode;
        this.sqlState = sqlState;
    }

    /**
     * @see ErrorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    public String getSqlState() {
        return sqlState;
    }
}
