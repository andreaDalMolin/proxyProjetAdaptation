package task;

import command.CommandType;

import java.net.InetAddress;

public class Task {
    private final InetAddress address;
    private final int port;
    private final CommandType commandType;
    private final Protocol protocol;

    public Task(InetAddress address, int port, CommandType type, Protocol protocol){
        this.address = address;
        this.port = port;
        this.commandType = type;
        this.protocol = protocol;
    }

    public CommandType getType(){
        return commandType;
    }

    public Protocol getProtocol(){
        return protocol;
    }

    public InetAddress getAddress(){
        return address;
    }

    public int getPort(){
        return port;
    }
}
