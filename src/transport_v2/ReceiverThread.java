package transport_v2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

public class ReceiverThread extends Thread {
    private final MulticastSocket transport;
    private final SocketImpl socket;

    private final HashSet<PacketListener> listeners = new HashSet<>();
    private final HashSet<Integer> handledPackets = new HashSet<>();

    public ReceiverThread(MulticastSocket transport, SocketImpl socket) {
        this.transport = transport;
        this.socket = socket;
    }

    public void addPacketListener(PacketListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removePacketListener(PacketListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        byte[] buffer = new byte[1 << 16];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try {
            while (true) {
                transport.receive(packet);

                RawPacket rawPacket = RawPacket.tryParse(Arrays.copyOfRange(packet.getData(), 0, packet.getLength()));

                if(rawPacket == null || rawPacket.getSourceIp().equals(socket.getIp()) || handledPackets.contains(rawPacket.hashCode())) {
                    continue;
                }

                System.out.println("received " + rawPacket);

                Iterable<PacketListener> listenersCopy;

                synchronized (listeners) {
                    listenersCopy = (Iterable<PacketListener>) listeners.clone();
                }

                for(PacketListener listener : listenersCopy) {
                    listener.onPacketReceived(rawPacket);
                }

                handledPackets.add(rawPacket.hashCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
