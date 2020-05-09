package com.replica.mysql.binlog.network;

import java.net.Socket;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;

public interface SSLSocketFactory {

    SSLSocket createSocket(Socket socket) throws SocketException;
}
