package transport;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalHandler implements PacketListener {
    private final SocketImpl socket;
    private final LinkedBlockingQueue<Packet> packetQueue;
    private SortedSet<Integer> received;
    private HashMap<Integer, Packet> receivedPackets;

    public LocalHandler(SocketImpl socket, LinkedBlockingQueue<Packet> packetQueue) {
        this.socket = socket;
        this.packetQueue = packetQueue;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(packet.isSyn() && !packet.isAck()) {
            return;
        }

        if(packet.getDestinationIp().equals(socket.getAddress())) {
            return;
        }

        if(packet.isAck() && !packet.isSyn()) { // ACK
            socket.gotAck(packet.getSourceIp(), packet.getAcknowledgmentNumber());
        } else if(!packet.isAck() && !packet.isSyn()) { // DATA
            if(socket.hasOtherClient(packet.getSourceIp())) {
                received.add(packet.getSequenceNumber());
                receivedPackets.put(packet.getSequenceNumber(), new PacketImpl(packet));

                while(received.size() > 0 && received.first() == socket.lastSequenceNumber(packet.getSourceIp()) + 1) {
                    int packetNo = received.first();
                    received.remove(packetNo);

                    Packet queuePacket = receivedPackets.remove(packetNo);

                    try {
                        packetQueue.put(queuePacket);
                    } catch (InterruptedException e) {  }
                }
            }
        }
    }
}
