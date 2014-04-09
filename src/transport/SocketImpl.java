package transport;

import exceptions.InvalidPacketException;

import javax.swing.undo.CannotRedoException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
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
    private static final int SYN_INTERVAL = 60*1000;
    private static final String BROADCAST_DESTINATION = "192.168.1.0";
    private static final long RETRANSMIT_DELAY = 1500;
    protected static final int MAX_RETRANSMITS = 5;

    private MulticastSocket mulSocket;

    private boolean connected = false;

    private InetAddress group;
    private int port;

    private ReceiverThread receiverThread;

    private LinkedBlockingQueue<Packet> packetQueue = new LinkedBlockingQueue<>();

    private HashSet<InetAddress> network = new HashSet<>();

    /**
     * The last sequence number just to send a packet with to the destination.
     */
    private final HashMap<InetAddress, Integer> sendLastSequenceNumbers = new HashMap<>();

    /**
     * The sequence number of the last packet, from the source, put in the packetQueue.
     * All packets with a lower sequence number have already been received.
     */
    private final HashMap<InetAddress, Integer> receivedLastSequenceNumbers = new HashMap<>();

    /**
     * Sequence numbers of send and not yet acked packets per destination.
     */
    private HashMap<InetAddress, HashSet<Integer>> unAckedSendPackets = new HashMap<>();

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
        if (connected) {
            throw new CannotRedoException();
        }

        syn();

        // Periodically repeat syn.
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                syn();
            }
        }, SYN_INTERVAL, SYN_INTERVAL);

        connected = true;
    }

    @SuppressWarnings("unchecked") // ignore unchecked casts after clone().
    private void syn() {
        HashSet<InetAddress> candidates = (HashSet<InetAddress>) network.clone();
        HashSet<InetAddress> verified = new HashSet<>();

        int tries = 0;

        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        SynAckHandler handler = new SynAckHandler(lock, condition, candidates, verified);
        while(tries < MAX_TRIES && (!candidates.equals(verified) || verified.size() == 0)) {
            lock.lock();
            receiverThread.addPacketListener(handler);


            try {
                send(new RawPacket(RawPacket.SYN_MASK, 0, 0, (int) System.currentTimeMillis(), getAddress(), InetAddress.getByName(BROADCAST_DESTINATION)));
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
        DatagramPacket datagram = new DatagramPacket(packet.getBytes(), packet.getLength(), group, port);

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
        int seq = -1;
        RawPacket packet = null;

        try {
            seq = getSequenceNumber(destination);
            packet = new RawPacket((byte) 0, seq, 0, 0, getAddress(), destination, data);
        } catch (InvalidPacketException e) {  }

        getUnAckedPackets(destination).add(seq);

        awaitAck(packet);

        send(packet);
    }

    protected void awaitAck(RawPacket packet) {
        new Timer().schedule(new RetransmitRunnable(this, packet, network), RETRANSMIT_DELAY);
    }

    private HashSet<Integer> getUnAckedPackets(InetAddress destination) {
        if(unAckedSendPackets.get(destination) == null) {
            unAckedSendPackets.put(destination, new HashSet<Integer>());
        }

        return unAckedSendPackets.get(destination);
    }

    @Override
    public void broadcast(byte[] data) {
        for(InetAddress address : network) {
            send(data, address);
        }
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
            if (!receivedLastSequenceNumbers.containsKey(sourceIp) || lastSequenceNumber(sourceIp) < sequenceNumber) {
                receivedLastSequenceNumbers.put(sourceIp, sequenceNumber);
            }
        }
    }

    public int lastSequenceNumber(InetAddress sourceIp) {
        return receivedLastSequenceNumbers.get(sourceIp);
    }

    public int getSequenceNumber(InetAddress destinationIp) {
        synchronized (sendLastSequenceNumbers) {
            int result = sendLastSequenceNumbers.get(destinationIp);
            sendLastSequenceNumbers.put(destinationIp, result + 1);
            return result;
        }
    }

    public void gotAck(InetAddress sourceIp, int acknowledgmentNumber) {
        if (unAckedSendPackets.containsKey(sourceIp)) {
            unAckedSendPackets.get(sourceIp).remove(acknowledgmentNumber);
        }
    }

    public boolean isAcked(InetAddress destination, int sequenceNumber) {
        return !getUnAckedPackets(destination).contains(sequenceNumber);
    }

    public void removeDestination(InetAddress ip) {
        synchronized (network) {
            network.remove(ip);
        }
        synchronized (unAckedSendPackets) {
            unAckedSendPackets.remove(ip);
        }
    }
}
