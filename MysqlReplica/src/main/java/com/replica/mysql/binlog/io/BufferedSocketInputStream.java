package com.replica.mysql.binlog.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedSocketInputStream extends FilterInputStream {

    private byte[] buffer;
    private int offset;
    private int limit;

    public BufferedSocketInputStream(InputStream in) {
        this(in, 512 * 1024);
    }

    public BufferedSocketInputStream(InputStream in, int bufferSize) {
        super(in);
        this.buffer = new byte[bufferSize];
    }

    @Override
    public int available() throws IOException {
        return limit - offset + in.available();
    }

    @Override
    public int read() throws IOException {
        if (offset < limit) {
            return buffer[offset++] & 0xff;
        }
        offset = 0;
        limit = in.read(buffer, 0, buffer.length);
        return limit != -1 ? buffer[offset++] & 0xff : -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (offset >= limit) {
            if (len >= buffer.length) {
                return in.read(b, off, len);
            }
            offset = 0;
            limit = in.read(buffer, 0, buffer.length);
        }
        int bytesRemainingInBuffer = Math.min(len, limit - offset);
        System.arraycopy(buffer, offset, b, off, bytesRemainingInBuffer);
        offset += bytesRemainingInBuffer;
        return bytesRemainingInBuffer;
    }

}