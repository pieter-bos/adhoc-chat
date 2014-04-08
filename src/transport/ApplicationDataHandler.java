package transport;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class ApplicationDataHandler implements PacketListener {
    private LinkedBlockingQueue<Packet> queue;

    public ApplicationDataHandler(LinkedBlockingQueue<Packet> queue) {
        this.queue = queue;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.isAck() && !packet.isSyn()) {
            try {
                new PacketImpl(packet.getData(), InetAddress.getByAddress(packet.getSourceAddress()), InetAddress.getByAddress(packet.getDestinationAddress()));
            } catch(UnknownHostException e) {  }
        }
    }
}
