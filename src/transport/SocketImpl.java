package transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SocketImpl implements Socket {
    private static final String GROUP = "224.224.224.224";
    private MulticastSocket mulSock;
    private boolean connected = false;

    public SocketImpl(int port) throws IOException {
        mulSock = new MulticastSocket(port);
        mulSock.joinGroup(InetAddress.getByName(GROUP));
    }

    @Override
    public void connect() {
        // TODO connect

        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
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
