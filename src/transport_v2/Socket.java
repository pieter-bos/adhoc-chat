package transport_v2;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Observable;

public abstract class Socket extends Observable {
    protected final int port;

    public Socket(int port) throws IOException {
        this.port = port;
    }

    public abstract void connect() throws IOException;
    public abstract boolean isConnected();
    public abstract void send(byte[] data, InetAddress destination) throws IOException;
    public abstract void broadcast(byte[] data) throws IOException;
    public abstract Packet receive() throws InterruptedException;
    public abstract Iterable<InetAddress> getOtherClients();
}