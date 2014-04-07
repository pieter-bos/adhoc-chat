package transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

public class ReceiverThread extends Thread {
    private MulticastSocket mulSocket;
    private Set<PacketListener> packetListeners = new HashSet<PacketListener>();

    public ReceiverThread(MulticastSocket socket) {
        mulSocket = socket;
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
    public void run() {
        byte[] buf = new byte[1<<16];
        try {
            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                mulSocket.receive(datagramPacket);

                RawPacket rawPacket = RawPacket.tryParse(datagramPacket.getData());

                if (rawPacket != null) {
                    synchronized (packetListeners) {
                        for (PacketListener listener : packetListeners) {
                            listener.onPacketReceived(rawPacket);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // STOP
        }
    }
}
