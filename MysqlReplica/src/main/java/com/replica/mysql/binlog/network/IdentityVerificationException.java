package com.replica.mysql.binlog.network;

import javax.net.ssl.SSLException;

public class IdentityVerificationException extends SSLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2204609050902003695L;

	public IdentityVerificationException(String message) {
		super(message);
	}

}