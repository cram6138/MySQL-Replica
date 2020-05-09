package com.replica.mysql.binlog.network;

public enum SSLMode {

	/**
	 * Establish a secure (encrypted) connection if the server supports secure
	 * connections. Fall back to an unencrypted connection otherwise.
	 */
	PREFERRED,
	/**
	 * Establish an unencrypted connection.
	 */
	DISABLED,
	/**
	 * Establish a secure connection if the server supports secure connections. The
	 * connection attempt fails if a secure connection cannot be established.
	 */
	REQUIRED,
	/**
	 * Like REQUIRED, but additionally verify the server TLS certificate against the
	 * configured Certificate Authority (CA) certificates. The connection attempt
	 * fails if no valid matching CA certificates are found.
	 */
	VERIFY_CA,
	/**
	 * Like VERIFY_CA, but additionally verify that the server certificate matches
	 * the host to which the connection is attempted.
	 */
	VERIFY_IDENTITY

}
