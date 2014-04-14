package transport_v2;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;

public class AnnounceHandler implements PacketListener {
    private final SocketImpl socket;
    private final HashMap<InetAddress, Long> timeLastAnnounceReceived;

    public AnnounceHandler(SocketImpl socket, HashMap<InetAddress, Long> timeLastAnnounceReceived) {
        this.socket = socket;
        this.timeLastAnnounceReceived = timeLastAnnounceReceived;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.isAnnounce()) {
            return;
        }

        synchronized (timeLastAnnounceReceived) {
            timeLastAnnounceReceived.put(packet.getSourceIp(), System.currentTimeMillis());
        }

        try {
            socket.sendAndAwaitAck(RawPacket.newSynchronization(socket.newSequenceNumber(packet.getSourceIp()), socket.getIp(), packet.getSourceIp()));
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}