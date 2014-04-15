package transport_v2;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketImpl implements Socket {
    private static final long ANNOUNCE_INTERVAL = 30 * 1000;
    private static final int MAX_ANNOUNCE_DROP_COUNT = 3;
    private static final long RETRANSMIT_INTERVAL = 1 * 1000;
    private static final int MAX_RETRANSMIT_COUNT = 5;
    private static String GROUP = "244.244.244.244";

    private final InetAddress ip;
    private final MulticastSocket transport;
    private boolean connected = false;
    private final LinkedBlockingQueue<Packet> receiveQueue = new LinkedBlockingQueue<>();
    private final ReceiverThread receiverThread;

    private final HashMap<InetAddress, Long> timeLastAnnounceReceived = new HashMap<>();
    private final HashMap<InetAddress, Integer> lastUsedSequenceNumber = new HashMap<>();
    private final HashMap<InetAddress, HashSet<RawPacket>> sentButNoAck = new HashMap<>();
    private final HashSet<InetAddress> network = new HashSet<>();

    public SocketImpl(int port) throws IOException {
        InetAddress ip = null;
        NetworkInterface netIF = null;

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements() && ip == null){
            NetworkInterface ni = interfaces.nextElement();
            if (ni.isUp() && !ni.isLoopback() && ni.supportsMulticast()) {
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

        transport = new MulticastSocket(port);
        transport.joinGroup(new InetSocketAddress(GROUP, port), netIF);

        receiverThread = new ReceiverThread(transport, this);

        receiverThread.addPacketListener(new AnnounceHandler(this, timeLastAnnounceReceived));
        receiverThread.addPacketListener(new SynchronizationHandler(this));
        receiverThread.addPacketListener(new BroadcastHandler(this));
        receiverThread.addPacketListener(new DataHandler(this, receiveQueue));

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
        }, 0, ANNOUNCE_INTERVAL);

        this.connected = true;
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    protected void send(RawPacket packet) throws IOException {
        transport.send(new DatagramPacket(packet.getBytes(), packet.getLength()));
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
            sentButNoAck.put(packet.getDestinationIp(), new HashSet<RawPacket>());
        }

        new Timer().schedule(new AckAwaitTimerTask(this, packet, sentButNoAck.get(packet.getDestinationIp()), retries), RETRANSMIT_INTERVAL);
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
    }

    @Override
    public void send(byte[] data, InetAddress destination) {
        synchronized (network) {
            if (!network.contains(destination)) {
                return; // TODO throw exception
            }
        }
        // TODO send packet
    }

    @Override
    public void broadcast(byte[] data) {

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
    public Iterable<InetAddress> getOtherClients() {
        // TODO implement
        return null;
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
    }
}
