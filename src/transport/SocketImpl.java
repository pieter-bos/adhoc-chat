package transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SocketImpl implements Socket {
    private static final String GROUP = "224.224.224.224";
    private MulticastSocket mulSocket;
    private boolean connected = false;

    public SocketImpl(int port) throws IOException {
        mulSocket = new MulticastSocket(port);
        mulSocket.joinGroup(InetAddress.getByName(GROUP));
    }

    @Override
    public void connect() {
        syn();

        connected = true;
    }

    private void syn() {
        // TODO implement this thing

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
