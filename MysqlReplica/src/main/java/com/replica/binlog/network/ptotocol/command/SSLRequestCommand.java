package com.replica.binlog.network.ptotocol.command;

import java.io.IOException;

import com.replica.mysql.binlog.io.ByteArrayOutputStream;
import com.replica.mysql.binlog.network.ClientCapabilities;

public class SSLRequestCommand implements Command {

    private int clientCapabilities;
    private int collation;

    public void setClientCapabilities(int clientCapabilities) {
        this.clientCapabilities = clientCapabilities;
    }

    public void setCollation(int collation) {
        this.collation = collation;
    }

    @Override
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int clientCapabilities = this.clientCapabilities;
        if (clientCapabilities == 0) {
            clientCapabilities = ClientCapabilities.LONG_FLAG |
                ClientCapabilities.PROTOCOL_41 | ClientCapabilities.SECURE_CONNECTION;
        }
        clientCapabilities |= ClientCapabilities.SSL;
        buffer.writeInteger(clientCapabilities, 4);
        buffer.writeInteger(0, 4); // maximum packet length
        buffer.writeInteger(collation, 1);
        for (int i = 0; i < 23; i++) {
            buffer.write(0);
        }
        return buffer.toByteArray();
    }

}
