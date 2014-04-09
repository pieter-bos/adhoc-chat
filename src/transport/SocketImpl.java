package transport;

import exceptions.InvalidPacketException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SocketImpl implements Socket {
    private static final String GROUP = "224.224.224.224";
    private static final int MAX_TRIES = 3;
    private static final long SYN_TIMEOUT = 2000;
    private static final TimeUnit SYN_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    private static final String BROADCAST_DESTINATION = "192.168.1.0";

    private MulticastSocket mulSocket;

    private boolean connected = false;

    private InetAddress group;
    private int port;

    private ReceiverThread receiverThread;

    private LinkedBlockingQueue<Packet> packetQueue = new LinkedBlockingQueue<>();

    private HashSet<InetAddress> network = new HashSet<>();

    private HashMap<InetAddress, Integer> sendLastSequenceNumbers = new HashMap<>();
    private HashMap<InetAddress, Integer> receivedLastSequenceNumbers = new HashMap<>();

    // Destination, (SequenceNumber, Packet)
    private HashMap<InetAddress, HashMap<Integer, Packet>> unAckedSendPackets = new HashMap<>();

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
        HashSet<InetAddress> candidates = (HashSet<InetAddress>) network.clone();
        HashSet<InetAddress> verified = new HashSet<>();

        int tries = 0;

        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        while(tries < MAX_TRIES && (!candidates.equals(verified) || verified.size() == 0)) {
            SynAckHandler handler = new SynAckHandler(lock, condition, candidates, verified, this);

            lock.lock();
            receiverThread.addPacketListener(handler);


            try {
                send(new RawPacket(RawPacket.SYN_MASK, 0, 0, tries, getAddress(), InetAddress.getByName(BROADCAST_DESTINATION)));
            } catch (InvalidPacketException | UnknownHostException e) {  }

            try {
                condition.await(SYN_TIMEOUT, SYN_TIMEOUT_UNIT);
            } catch (InterruptedException e) {  }

            receiverThread.removePacketListener(handler);
            lock.unlock();

            tries++;
        }

        network = (HashSet<InetAddress>) verified.clone();
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

    @Override
    public Iterable<InetAddress> getOtherClients() {
        return network;
    }

    public boolean hasOtherClient(InetAddress client) {
        return network.contains(client);
    }

    public void makeSequenceNumber(InetAddress sourceIp) {
        synchronized (sendLastSequenceNumbers) {
            if (!sendLastSequenceNumbers.containsKey(sourceIp)) {
                sendLastSequenceNumbers.put(sourceIp, 0);
            }
        }
    }

    public void gotSequenceNumber(InetAddress sourceIp, int sequenceNumber) {
        synchronized (receivedLastSequenceNumbers) {
            receivedLastSequenceNumbers.put(sourceIp, sequenceNumber);
        }
    }

    public int lastSequenceNumber(InetAddress sourceIp) {
        return receivedLastSequenceNumbers.get(sourceIp);
    }

    public int getSequenceNumber(InetAddress sourceIp) {
        synchronized (sendLastSequenceNumbers) {
            int result = sendLastSequenceNumbers.get(sourceIp);
            sendLastSequenceNumbers.put(sourceIp, result + 1);
            return result;
        }
    }

    public void gotAck(InetAddress sourceIp, int acknowledgmentNumber) {
        if (unAckedSendPackets.containsKey(sourceIp)) {
            unAckedSendPackets.get(sourceIp).remove(acknowledgmentNumber);
        }
    }
}
