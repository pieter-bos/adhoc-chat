package transport;

/**
 * Created by pieter on 4/7/14.
 */
public interface Socket {
    // public Socket(int port);

    public void connect();
    public boolean isConnected();
    public void send(Packet packet);
    public Packet receive();
}
