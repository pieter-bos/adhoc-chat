package transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SocketImpl implements Socket {
    private static final String GROUP = "224.224.224.224";
    private MulticastSocket mulSock;

    public SocketImpl(int port) throws IOException {
        mulSock = new MulticastSocket(port);
        mulSock.joinGroup(InetAddress.getByName(GROUP));
    }

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
