package com.replica.binlog.network.ptotocol.command;

import java.io.IOException;

import com.replica.mysql.binlog.io.ByteArrayOutputStream;

public class PingCommand implements Command {

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.writeInteger(CommandType.PING.ordinal(), 1);
        return buffer.toByteArray();
    }

}