package com.replica.binlog.network.ptotocol.command;

import java.io.IOException;

import com.replica.mysql.binlog.io.ByteArrayOutputStream;

public class QueryCommand implements Command {

    private String sql;

    public QueryCommand(String sql) {
        this.sql = sql;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.writeInteger(CommandType.QUERY.ordinal(), 1);
        buffer.writeString(this.sql);
        return buffer.toByteArray();
    }

}
