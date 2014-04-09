package transport;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class BroadcastHandler implements PacketListener {
    private SocketImpl socket;

    /**
     * Hashes of already forwarded packets.
     */
    private HashSet<Integer> packets = new HashSet<>();

    public BroadcastHandler(SocketImpl socket) {
        this.socket = socket;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!Arrays.equals(packet.getDestinationAddress(), socket.getAddress().getAddress())) {
            if(!packets.contains(packet.hashCode())) {
                packets.add(packet.hashCode());
                socket.send(packet);
            }
        }
    }
}
