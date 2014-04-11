package transport;

import exceptions.InvalidPacketException;

import javax.swing.undo.CannotRedoException;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SocketImpl implements Socket {
    /** The multicast group to join */
    private static final String GROUP = "224.224.224.224";
    /** Maximum number of syn retries. */
    private static final int MAX_TRIES = 3;
    /** Amount of time to wait between syn retries. */
    private static final long SYN_TIMEOUT = 2000;
    /** The time unit for <code>SYN_TIMEOUT</code>. */
    private static final TimeUnit SYN_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;
    /** The interval for periodically repeating syn. */
    private static final int SYN_INTERVAL = 60*1000;
    /** The IP address to use for broadcast. */
    private static final String BROADCAST_DESTINATION = "192.168.5.0";
    /** Timeout for retransmitting data packets. */
    private static final long RETRANSMIT_DELAY = 1500;
    /** Maximum number of retransmits of data packets. */
    protected static final int MAX_RETRANSMITS = 5;

    /** The <code>MulticastSocket</code> used. */
    private MulticastSocket mulSocket;

    /** Indicates if a connection is established and first syn send. */
    private boolean connected = false;

    /** The multicast group to join */
    private InetAddress group;
    /** The port number used. */
    private int port;

    /** Instance of <code>ReceiverThread</code> used. */
    private ReceiverThread receiverThread;

    /** Queue containing all packets ready for the application to read. */
    private LinkedBlockingQueue<Packet> packetQueue = new LinkedBlockingQueue<>();

    /** Contains the address of all known other clients in the network. */
    private HashSet<InetAddress> network = new HashSet<>();

    /** The last sequence number just to send a packet with to the destination. */
    private final HashMap<InetAddress, Integer> sendLastSequenceNumbers = new HashMap<>();

    /**
     * The sequence number of the last packet, from the source, put in the packetQueue.
     * All packets with a lower sequence number have already been received.
     */
    private final HashMap<InetAddress, Integer> receivedLastSequenceNumbers = new HashMap<>();

    /** Sequence numbers of send packets per destination for which no acknowledgement had been received. */
    private HashMap<InetAddress, HashSet<Integer>> unAckedSendPackets = new HashMap<>();


    /**
     * Create a new socket and bind it to a specific port.
     * @param port port to use
     * @throws IOException if an I/O exception occurs while creating the <code>SocketImpl</code>
     */
    public SocketImpl(int port) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(GROUP);

        mulSocket = new MulticastSocket(new InetSocketAddress(InetAddress.getLocalHost(), port));
        mulSocket.joinGroup(group);

        receiverThread = new ReceiverThread(mulSocket);

        receiverThread.addPacketListener(new PacketListener() {
            @Override
            public void onPacketReceived(RawPacket packet) {
                System.out.println("recv " + packet);
            }
        });
        receiverThread.addPacketListener(new SynHandler(this, network));
        receiverThread.addPacketListener(new BroadcastHandler(this));
        receiverThread.addPacketListener(new LocalHandler(this, packetQueue));

        receiverThread.start();
    }

    /**
     * Gets the local address to which the socket is bound.
     * @return the local address to which the socket is bound
     */
    public InetAddress getAddress() {
        return ((InetSocketAddress)(mulSocket.getLocalSocketAddress())).getAddress();
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

    /**
     * Broadcasts a SYN and waits for SYN ACK.
     */
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
                send(new RawPacket(RawPacket.SYN_MASK, 0, 0, (int) System.currentTimeMillis(),
                        getAddress(), InetAddress.getByName(BROADCAST_DESTINATION)));
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

    /**
     * Send a <code>RawPacket</code> through the <code>MulticastSocket</code> after
     * encapsulating it in a <code>DatagramPacket</code>.
     * @param packet the <code>RawPacket</code> to send.
     */
    protected synchronized void send(RawPacket packet) {
        System.out.println("send " + packet);
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

    /**
     * Schedules a new <code>RetransmitRunnable</code> to retransmit the packet if a ack has not been received after
     * <code>RETRANSMIT_DELAY</code> milliseconds..
     * @param packet
     */
    protected void awaitAck(RawPacket packet) {
        new Timer().schedule(new RetransmitRunnable(this, packet, network), RETRANSMIT_DELAY);
    }

    /**
     * Get all packets send to the specified destination for which no ack has been received.
     * @param destination the destination for which to get the packets
     * @return A set containing all packets awaiting acknowledgements send to the specified destination
     */
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

    /**
     * Checks whether the specified client is known to be in the network.
     * @param client the <code>InetAddress</code> to check
     * @return <code>true</code> if the client is known to be in the network; otherwise <code>false</code>
     */
    public boolean hasOtherClient(InetAddress client) {
        return network.contains(client);
    }

    /**
     * Register that a packet with the given sequence number, and all sequence numbers lower, has been received from
     * the specified source.
     * @param sourceIp the source
     * @param sequenceNumber the sequence number
     */
    public void gotSequenceNumber(InetAddress sourceIp, int sequenceNumber) {
        synchronized (receivedLastSequenceNumbers) {
            if (!receivedLastSequenceNumbers.containsKey(sourceIp) || lastSequenceNumber(sourceIp) < sequenceNumber) {
                receivedLastSequenceNumbers.put(sourceIp, sequenceNumber);
            }
        }
    }

    /**
     * Gets the last(/highest) sequence number that has been received from the specified source.
     * @param sourceIp the source
     * @return the sequence number of the last in order received packet
     */
    public int lastSequenceNumber(InetAddress sourceIp) {
        return receivedLastSequenceNumbers.get(sourceIp);
    }

    /**
     * Initializes a sequence number to use for packets to the specified destination.
     * @param destinationIp the destination for which to initialize a sequence number
     */
    public void makeSequenceNumber(InetAddress destinationIp) {
        synchronized (sendLastSequenceNumbers) {
            if (!sendLastSequenceNumbers.containsKey(destinationIp)) {
                // Initialize sequence number to 1 because SYN has 0.
                sendLastSequenceNumbers.put(destinationIp, 1);
            }
        }
    }

    /**
     * Get a sequence number for a packet to be send to the specified destination.
     * The retrieved sequence number must be send to the destination.
     * @param destinationIp the destination
     * @return a unique sequence number for a packet to the specified destination
     */
    public int getSequenceNumber(InetAddress destinationIp) {
        synchronized (sendLastSequenceNumbers) {
            int result = sendLastSequenceNumbers.get(destinationIp);
            sendLastSequenceNumbers.put(destinationIp, result + 1);
            return result;
        }
    }

    /**
     * Register that an ACK from the specified source has been received.
     * @param sourceIp the source
     * @param acknowledgementNumber the acknowledged sequence number
     */
    public void gotAck(InetAddress sourceIp, int acknowledgementNumber) {
        if (unAckedSendPackets.containsKey(sourceIp)) {
            unAckedSendPackets.get(sourceIp).remove(acknowledgementNumber);
        }
    }

    /**
     * Indicates if an ACK has been received for a packet send to the specified destination
     * with the specified sequence number.
     * @param destination the destination
     * @param sequenceNumber the sequence number
     * @return <code>false</code> if an acknowledgement is being awaited for a packet send to specified destination
     * with the specified sequence number; otherwise <code>true</code>
     */
    public boolean isAcked(InetAddress destination, int sequenceNumber) {
        return !getUnAckedPackets(destination).contains(sequenceNumber);
    }

    /**
     * Removes a client from the known network.
     * @param ip the IP of the client to be removed
     */
    public void removeDestination(InetAddress ip) {
        synchronized (network) {
            network.remove(ip);
        }
        synchronized (unAckedSendPackets) {
            unAckedSendPackets.remove(ip);
        }
    }
}
