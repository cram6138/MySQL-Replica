package com.replica.binlog.network.ptotocol.command;

import java.io.IOException;

public class AuthenticateNativePasswordCommand implements Command {
    private final String scramble, password;

    public AuthenticateNativePasswordCommand(String scramble, String password) {
        this.scramble = scramble;
        this.password = password;
    }
    @Override
    public byte[] toByteArray() throws IOException {
        return AuthenticateCommand.passwordCompatibleWithMySQL411(password, scramble);
    }
}