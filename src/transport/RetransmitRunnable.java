package transport;

import exceptions.InvalidPacketException;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.TimerTask;

public class RetransmitRunnable extends TimerTask {
    private SocketImpl socket;
    private RawPacket packet;
    private HashSet<InetAddress> network;

    public RetransmitRunnable(SocketImpl socket, RawPacket packet, HashSet<InetAddress> network) {
        this.socket = socket;
        this.packet = packet;
        this.network = network;
    }

    @Override
    public void run() {
        if(socket.isAcked(packet.getDestinationIp(), packet.getSequenceNumber())) {
            return;
        }

        if(packet.getRetransmissionNumber() >= SocketImpl.MAX_RETRANSMITS) {
            socket.removeDestination(packet.getDestinationIp());
        } else {
            try {
                RawPacket retransmit = new RawPacket(packet.getFlags(), packet.getSequenceNumber(),
                        packet.getAcknowledgmentNumber(), packet.getRetransmissionNumber() + 1,
                        packet.getSourceAddress(), packet.getDestinationAddress(), packet.getData());
                socket.send(retransmit);
                socket.awaitAck(retransmit);
            } catch (InvalidPacketException e) {  }
        }
    }
}
