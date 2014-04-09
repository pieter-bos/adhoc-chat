package transport;

import exceptions.InvalidPacketException;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalHandler implements PacketListener {
    private final SocketImpl socket;
    private final LinkedBlockingQueue<Packet> packetQueue;
    // All (out of order) received sequence numbers
    private SortedSet<Integer> received;
    // All (out of order) received packets
    private HashMap<Integer, Packet> receivedPackets;

    public LocalHandler(SocketImpl socket, LinkedBlockingQueue<Packet> packetQueue) {
        this.socket = socket;
        this.packetQueue = packetQueue;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        // Syn and SynAck are handled elsewhere.
        if(packet.isSyn()) {
            return;
        }

        // Packets for others are send on elsewhere
        if(!packet.getDestinationIp().equals(socket.getAddress())) {
            return;
        }

        if(packet.isAck()) { // ACK
            socket.gotAck(packet.getSourceIp(), packet.getAcknowledgmentNumber());
        } else if(!packet.isAck()) { // DATA
            if(socket.hasOtherClient(packet.getSourceIp())) {
                received.add(packet.getSequenceNumber());
                receivedPackets.put(packet.getSequenceNumber(), new PacketImpl(packet));

                // Send Ack
                try {
                    socket.send(new RawPacket(RawPacket.ACK_MASK, socket.getSequenceNumber(packet.getSourceIp()),
                            packet.getSequenceNumber(), 0, socket.getAddress(), packet.getSourceIp()));
                } catch (InvalidPacketException e) {  }

                while(received.size() > 0 && received.first() == socket.lastSequenceNumber(packet.getSourceIp()) + 1) {
                    int packetNo = received.first();
                    received.remove(packetNo);

                    Packet queuePacket = receivedPackets.remove(packetNo);

                    try {
                        packetQueue.put(queuePacket);
                        socket.gotSequenceNumber(queuePacket.getSourceAddress(), packetNo);

                        /*
                        // Send Ack
                        socket.send(new RawPacket(RawPacket.ACK_MASK,
                                socket.getSequenceNumber(queuePacket.getSourceAddress()),
                                packetNo, 0, socket.getAddress(), packet.getSourceIp()));
                                */
                    } catch (InterruptedException e) {  } // catch (InvalidPacketException e) {  }
                }
            }
        }
    }
}
