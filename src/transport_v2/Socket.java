package transport_v2;

import java.io.IOException;
import java.net.InetAddress;

public interface Socket {
    // public Socket(int port);

    public void connect() throws IOException;
    public boolean isConnected();
    public void send(byte[] data, InetAddress destination) throws IOException;
    public void broadcast(byte[] data) throws IOException;
    public Packet receive() throws InterruptedException;
    public Iterable<InetAddress> getOtherClients();
}