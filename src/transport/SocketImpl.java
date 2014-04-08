package transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketImpl implements Socket {
    private static final String GROUP = "224.224.224.224";

    private MulticastSocket mulSocket;

    private boolean connected = false;

    private InetAddress group;
    private int port;

    private ReceiverThread receiverThread;

    private LinkedBlockingQueue<Packet> packetQueue = new LinkedBlockingQueue<>();

    private HashSet<InetAddress> network = new HashSet<>();

    private HashMap<InetAddress, Integer> sendSequenceNumbers = new HashMap<>();
    private HashMap<InetAddress, Integer> receivedSequenceNumbers = new HashMap<>();

    private Random random = new SecureRandom();

    public SocketImpl(int port) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(GROUP);

        mulSocket = new MulticastSocket(port);
        mulSocket.joinGroup(group);

        receiverThread = new ReceiverThread(mulSocket);

        receiverThread.addPacketListener(new SynHandler(this, network));
        receiverThread.addPacketListener(new BroadcastHandler(this));
        receiverThread.addPacketListener(new LocalHandler(this, packetQueue));

        receiverThread.start();
    }

    public InetAddress getAddress() {
        return mulSocket.getLocalAddress();
    }

    @Override
    public void connect() {
        syn();

        connected = true;
    }

    private void syn() {

    }

    protected synchronized void send(RawPacket packet) {
        DatagramPacket datagram = new DatagramPacket(packet.getData(), packet.getLength(), group, port);

        try {
            mulSocket.send(datagram);
        } catch(IOException e) {
            connected = false;
        }
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

    public int randInt() {
        synchronized (random) {
            return random.nextInt(1000000);
        }
    }

    @Override
    public Iterable<InetAddress> getOtherClients() {
        return network;
    }

    public boolean hasOtherClient(InetAddress client) {
        return network.contains(client);
    }

    public void makeSequenceNumber(InetAddress sourceIp) {
        synchronized (sendSequenceNumbers) {
            if (!sendSequenceNumbers.containsKey(sourceIp)) {
                sendSequenceNumbers.put(sourceIp, randInt());
            }
        }
    }

    public void gotSequenceNumber(InetAddress sourceIp, int sequenceNumber) {
        synchronized (receivedSequenceNumbers) {
            receivedSequenceNumbers.put(sourceIp, sequenceNumber);
        }
    }

    public int lastSequenceNumber(InetAddress sourceIp) {
        return receivedSequenceNumbers.get(sourceIp);
    }

    public int getSequenceNumber(InetAddress sourceIp) {
        synchronized (sendSequenceNumbers) {
            int result = sendSequenceNumbers.get(sourceIp);
            sendSequenceNumbers.put(sourceIp, result + 1);
            return result;
        }
    }
}
