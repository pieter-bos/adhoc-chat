package transport_v2;

import transport.Packet;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketImpl implements Socket {
    private static final long ANNOUNCE_INTERVAL = 30 * 1000;
    private static String GROUP = "244.244.244.244";

    private final InetAddress ip;
    private final MulticastSocket transport;
    private boolean connected = false;
    private final LinkedBlockingQueue<Packet> receiveQueue = new LinkedBlockingQueue<>();
    private final ReceiverThread receiverThread;


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



        receiverThread.start();
    }

    @Override
    public void connect() throws IOException {
        send(RawPacket.newAnnounce(getIp()));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    send(RawPacket.newAnnounce(getIp()));
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

    @Override
    public void send(byte[] data, InetAddress destination) {

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

    }

    public InetAddress getIp() {
        return ip;
    }
}
