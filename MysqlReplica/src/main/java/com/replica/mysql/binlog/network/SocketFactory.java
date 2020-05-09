package com.replica.mysql.binlog.network;

import java.net.Socket;
import java.net.SocketException;

public interface SocketFactory {

	Socket createSocket() throws SocketException;
}