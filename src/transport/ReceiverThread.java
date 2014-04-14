package transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReceiverThread extends Thread {
    private MulticastSocket mulSocket;
    private SocketImpl socket;
    private HashSet<PacketListener> packetListeners = new HashSet<>();

    public ReceiverThread(MulticastSocket mulSocket, SocketImpl socket) {
        this.mulSocket = mulSocket;
        this.socket = socket;
    }

    public void addPacketListener(PacketListener listener) {
        synchronized (packetListeners) {
            packetListeners.add(listener);
        }
    }

    public void removePacketListener(PacketListener listener) {
        synchronized (packetListeners) {
            packetListeners.remove(listener);
        }
    }

    @Override
    @SuppressWarnings("unchecked") // ignore unchecked casts after clone().
    public void run() {
        byte[] buf = new byte[1<<16];
        try {
            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                mulSocket.receive(datagramPacket);

                byte[] packet = Arrays.copyOfRange(datagramPacket.getData(), 0, datagramPacket.getLength());

                RawPacket rawPacket = RawPacket.tryParse(packet);

                if (rawPacket != null && !socket.hasSend(rawPacket)) {
                    HashSet<PacketListener> listeners;
                    synchronized (packetListeners) {
                        listeners = (HashSet<PacketListener>) packetListeners.clone();
                    }
                    for (PacketListener listener : listeners) {
                        listener.onPacketReceived(rawPacket);
                    }
                }
            }
        } catch (IOException e) {
            // STOP
        }
    }
}
