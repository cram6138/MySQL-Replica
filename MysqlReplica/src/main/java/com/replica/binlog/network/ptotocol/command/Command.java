package com.replica.binlog.network.ptotocol.command;

import java.io.IOException;

import com.replica.mysql.binlog.network.Packet;

public interface Command extends Packet {

    byte[] toByteArray() throws IOException;

}