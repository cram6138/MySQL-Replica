package com.replica.mysql.binlog.network;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

public class DefaultSSLSocketFactory implements SSLSocketFactory {

    private final String protocol;

    public DefaultSSLSocketFactory() {
        this("TLSv1");
    }

    /**
     * @param protocol TLSv1, TLSv1.1 or TLSv1.2 (the last two require JDK 7+)
     */
    public DefaultSSLSocketFactory(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public SSLSocket createSocket(Socket socket) throws SocketException {
        SSLContext sc;
        try {
            sc = SSLContext.getInstance(this.protocol);
            initSSLContext(sc);
        } catch (GeneralSecurityException e) {
            throw new SocketException(e.getMessage());
        }
        try {
            return (SSLSocket) sc.getSocketFactory()
                .createSocket(socket, socket.getInetAddress().getHostName(), socket.getPort(), true);
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    protected void initSSLContext(SSLContext sc) throws GeneralSecurityException {
        sc.init(null, null, null);
    }

}
