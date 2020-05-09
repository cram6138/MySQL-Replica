
package com.replica.mysql.binlog.network;

/**
 * @author bhajuram.c
 */
public class AuthenticationException extends ServerException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -385810533986926044L;

	public AuthenticationException(String message, int errorCode, String sqlState) {
        super(message, errorCode, sqlState);
    }

    public AuthenticationException(String message) {
        super(message, 0, "HY000");
    }
}
