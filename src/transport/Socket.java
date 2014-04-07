package transport;

import java.net.InetAddress;

public interface Socket {
    // public Socket(int port);

    public void connect();
    public boolean isConnected();
    public void send(byte[] data, InetAddress destination);
    public void broadcast(byte[] data);
    public Packet receive();
}