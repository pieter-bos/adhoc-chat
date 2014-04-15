package transport_v2;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class DataHandler implements PacketListener {
    private static final long MAX_DIFFERENCE = 5;
    private final SocketImpl socket;
    private final LinkedBlockingQueue<Packet> queue;
    private HashMap<InetAddress, HashSet<Integer>> sentButNoAck;
    private final HashMap<InetAddress, Integer> lastInOrderSequenceNumber = new HashMap<>();
    private final HashMap<InetAddress, SortedSet<RawPacket>> outOfOrderPackets = new HashMap<>();

    public DataHandler(SocketImpl socket, LinkedBlockingQueue<Packet> queue, HashMap<InetAddress, HashSet<Integer>> sentButNoAck) {
        this.socket = socket;
        this.queue = queue;
        this.sentButNoAck = sentButNoAck;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.getDestinationIp().equals(socket.getIp())) {
            return;
        }

        if (!lastInOrderSequenceNumber.containsKey(packet.getSourceIp()) || !outOfOrderPackets.containsKey(packet.getSourceIp())) {
            if (packet.isSyn()) {
                lastInOrderSequenceNumber.put(packet.getSourceIp(), packet.getSequenceNumber());
                outOfOrderPackets.put(packet.getSourceIp(), new TreeSet<RawPacket>());
            }

            return; // Drop data until SYN is received
        }

        if(packet.isSyn()) {
            // If the sequence number of the SYN is lower than the last in order sequence number or
            //  there is a packet received out of order with a sequence number more than MAX_DIFFERENCE higher
            //  then the other client is suspected of having restarted.
            if (Util.differenceWithWrapAround(packet.getSequenceNumber(), lastInOrderSequenceNumber.get(packet.getSourceIp())) < 0 ||
                    !outOfOrderPackets.get(packet.getSourceIp()).tailSet(packet).isEmpty() &&
                            Util.differenceWithWrapAround(packet.getSequenceNumber(), outOfOrderPackets.get(packet.getSourceIp()).last().getSequenceNumber()) < -MAX_DIFFERENCE) {
                lastInOrderSequenceNumber.put(packet.getSourceIp(), packet.getSequenceNumber());
                outOfOrderPackets.put(packet.getSourceIp(), new TreeSet<RawPacket>());
                sentButNoAck.put(packet.getSourceIp(), new HashSet<Integer>());
            } else {
                outOfOrderPackets.get(packet.getSourceIp()).add(packet);
            }
        } else if (!packet.isAck() && !packet.isAnnounce()) {
            // i.e. it is a data packet
            if(Util.differenceWithWrapAround(packet.getSequenceNumber(), lastInOrderSequenceNumber.get(packet.getSourceIp())) > 0) {
                outOfOrderPackets.get(packet.getSourceIp()).add(packet);
            }

            try {
                socket.send(RawPacket.newAcknowledgement(packet.getSequenceNumber(), socket.getIp(), packet.getSourceIp()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Iterator<RawPacket> it = outOfOrderPackets.get(packet.getSourceIp()).iterator();
        while (it.hasNext()) {
            RawPacket cur = it.next();
            if (cur.getSequenceNumber() == lastInOrderSequenceNumber.get(packet.getSourceIp()) +1) {
                if (!cur.isSyn()) {
                    queue.add(new PacketImpl(cur));
                }
                lastInOrderSequenceNumber.put(packet.getSourceIp(), cur.getSequenceNumber());
                it.remove();
            } else {
                break;
            }
        }
    }
}