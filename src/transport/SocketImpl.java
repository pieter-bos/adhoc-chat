package transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketImpl implements Socket {
    private static final String GROUP = "224.224.224.224";
    private MulticastSocket mulSocket;
    private boolean connected = false;

    private InetAddress group;
    private int port;

    private ReceiverThread receiverThread;

    private LinkedBlockingQueue<Packet> packetQueue;

    public SocketImpl(int port) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(GROUP);

        mulSocket = new MulticastSocket(port);
        mulSocket.joinGroup(group);

        receiverThread = new ReceiverThread(mulSocket);

        receiverThread.addPacketListener(new ApplicationDataHandler(packetQueue));

        receiverThread.start();
    }

    @Override
    public void connect() {
        syn();

        connected = true;
    }

    private void syn() {

    }

    private void send(RawPacket packet) throws IOException {
        DatagramPacket datagram = new DatagramPacket(packet.getData(), packet.getLength(), group, port);
        mulSocket.send(datagram);
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
        try {
            return packetQueue.take();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    public Iterable<InetAddress> getOtherClients() {
        return null;
    }
}
