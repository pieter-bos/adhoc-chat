package transport_v2;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

public class DataHandler implements PacketListener {
    private final SocketImpl socket;
    private final LinkedBlockingQueue<Packet> queue;
    private final SortedSet<RawPacket> outOfOrderPackets = new TreeSet<>();

    public DataHandler(SocketImpl socket, LinkedBlockingQueue<Packet> queue) {
        this.socket = socket;
        this.queue = queue;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.getDestinationIp().equals(socket.getIp())) {
            return;
        }

        if(packet.isSyn()) {
            for(RawPacket toRemove : outOfOrderPackets.tailSet(packet)) {
                outOfOrderPackets.remove(toRemove);
            }
        }
    }
}