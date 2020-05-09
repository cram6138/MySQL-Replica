package com.replica.mysql.binlog.network;

public interface Packet {

	// https://dev.mysql.com/doc/internals/en/sending-more-than-16mbyte.html
	int MAX_LENGTH = 16777215;
}