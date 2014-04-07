package transport;

import java.net.InetAddress;

public class SocketImpl implements Socket {
    @Override
    public void connect() {
        
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void send(byte[] data, InetAddress destination) {

    }

    @Override
    public void broadcast(byte[] data) {

    }

    @Override
    public Packet receive() {
        return null;
    }

    @Override
    public Iterable<InetAddress> getOtherClients() {
        return null;
    }
}
