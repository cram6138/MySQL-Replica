package com.replica.mysql.binlog.network;


import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public class TLSHostnameVerifier implements HostnameVerifier {

    public boolean verify(String hostname, SSLSession session) {
        // HostnameChecker checker = HostnameChecker.getInstance(HostnameChecker.TYPE_TLS);
        try {
            Certificate[] peerCertificates = session.getPeerCertificates();
            if (peerCertificates.length > 0 && peerCertificates[0] instanceof X509Certificate) {
                X509Certificate peerCertificate = (X509Certificate) peerCertificates[0];
//                try {
//                    // checker.match(hostname, peerCertificate);
//                    return true;
//                } catch (CertificateException ignored) {
//                }
            }
        } catch (SSLPeerUnverifiedException ignored) {
        }
        return false;
    }

}
