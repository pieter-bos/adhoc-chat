package transport_v2;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketImpl extends Socket {
    public static final int MAX_DATA_SIZE = (1 << 16) - RawPacket.MIN_SIZE;

    private static final long ANNOUNCE_INTERVAL = 30 * 1000;
    private static final int MAX_ANNOUNCE_DROP_COUNT = 3;
    private static final long RETRANSMIT_INTERVAL = 1 * 1000;
    private static final int MAX_RETRANSMIT_COUNT = 5;
    private static final String GROUP = "224.224.224.224";

    private final InetAddress ip;
    private final MulticastSocket transport;
    private final InetAddress group;
    private boolean connected = false;
    private final LinkedBlockingQueue<Packet> receiveQueue = new LinkedBlockingQueue<>();
    private final ReceiverThread receiverThread;

    private final HashMap<InetAddress, Long> timeLastAnnounceReceived = new HashMap<>();
    private final HashMap<InetAddress, Integer> lastUsedSequenceNumber = new HashMap<>();
    private final HashMap<InetAddress, HashSet<Integer>> sentButNoAck = new HashMap<>();
    private final HashMap<InetAddress, HashSet<Integer>> sentSynAckButNoAck = new HashMap<>();
    private final HashSet<InetAddress> network = new HashSet<>();

    public SocketImpl(int port) throws IOException {
        super(port);
        this.group = InetAddress.getByName(GROUP);

        InetAddress ip = null;
        NetworkInterface netIF = null;

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements() && ip == null){
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.isLoopback() && ni.supportsMulticast()) {
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements() && ip == null) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        ip = address;
                        netIF = ni;
                    }
                }
            }
        }

        this.ip = ip;

        if (ip == null) {
            throw new IOException("No suitable interface found");
        }

        transport = new MulticastSocket(port);
        transport.joinGroup(new InetSocketAddress(group, port), netIF);

        receiverThread = new ReceiverThread(transport, this);

        receiverThread.addPacketListener(new AnnounceHandler(this, timeLastAnnounceReceived));
        receiverThread.addPacketListener(new SynchronizationHandler(this, sentSynAckButNoAck));
        receiverThread.addPacketListener(new BroadcastHandler(this));
        receiverThread.addPacketListener(new DataHandler(this, receiveQueue, sentButNoAck));
        receiverThread.addPacketListener(new AcknowledgementHandler(this, sentButNoAck, sentSynAckButNoAck));

        receiverThread.start();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void connect() throws IOException {
        send(RawPacket.newAnnounce(getIp()));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    send(RawPacket.newAnnounce(getIp()));

                    synchronized (timeLastAnnounceReceived) {
                        Iterator<Map.Entry<InetAddress, Long>> it = timeLastAnnounceReceived.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<InetAddress, Long> entry = it.next();
                            if (System.currentTimeMillis() - entry.getValue() > ANNOUNCE_INTERVAL * MAX_ANNOUNCE_DROP_COUNT) {
                                it.remove();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, ANNOUNCE_INTERVAL, ANNOUNCE_INTERVAL);

        this.connected = true;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    protected void send(RawPacket packet) throws IOException {
        System.out.println("send " + packet);
        DatagramPacket datagram = new DatagramPacket(packet.getBytes(), packet.getLength(), group, port);
        transport.send(datagram);
    }

    protected void sendAndAwaitAck(RawPacket packet) throws IOException {
        sendAndRetry(packet, MAX_RETRANSMIT_COUNT);
    }

    protected void sendAndRetry(RawPacket packet, int retries) throws IOException {
        if(retries <= 0) {
            removeFromNetwork(packet.getDestinationIp());
            return;
        }

        send(packet);

        if(!sentButNoAck.containsKey(packet.getDestinationIp())) {
            sentButNoAck.put(packet.getDestinationIp(), new HashSet<Integer>());
        }

        sentButNoAck.get(packet.getDestinationIp()).add(packet.getSequenceNumber());

        new Timer().schedule(new AckAwaitTimerTask(this, packet, sentButNoAck, retries), RETRANSMIT_INTERVAL);
    }

    protected void removeFromNetwork(InetAddress ip) {
        synchronized(sentButNoAck) {
            sentButNoAck.remove(ip);
        }

        synchronized(lastUsedSequenceNumber) {
            lastUsedSequenceNumber.remove(ip);
        }

        synchronized(timeLastAnnounceReceived) {
            timeLastAnnounceReceived.remove(ip);
        }

        synchronized(network) {
            network.remove(ip);
        }

        setChanged();
        notifyObservers();
    }

    @Override
    public void send(byte[] data, InetAddress destination) throws IOException {
        if (data.length > MAX_DATA_SIZE) {
            throw new IOException("To much data to send");
        }

        synchronized (network) {
            if (!network.contains(destination)) {
                throw new IOException("Destination not in current network");
            }
        }

        sendAndAwaitAck(RawPacket.newData(newSequenceNumber(destination), getIp(), destination, data));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void broadcast(byte[] data) throws IOException {
        if (data.length > MAX_DATA_SIZE) {
            throw new IOException("To much data to send");
        }

        Iterable<InetAddress> networkClone;

        synchronized(network) {
            networkClone = (Iterable<InetAddress>) network.clone();
        }

        for(InetAddress destination : networkClone) {
            send(data, destination);
        }
    }

    @Override
    public Packet receive() {
        try {
            return receiveQueue.take();
        } catch(InterruptedException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public HashSet<InetAddress> getOtherClients() {
        synchronized(network) {
            return (HashSet<InetAddress>) network.clone();
        }
    }

    protected int newSequenceNumber(InetAddress destination) {
        if(!lastUsedSequenceNumber.containsKey(destination)) {
            lastUsedSequenceNumber.put(destination, 0);
        }

        int result = lastUsedSequenceNumber.get(destination);
        lastUsedSequenceNumber.put(destination, result + 1);
        return result;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void addToNetwork(InetAddress ip) {
        synchronized (network) {
            network.add(ip);
        }

        setChanged();
        notifyObservers();
    }
}
