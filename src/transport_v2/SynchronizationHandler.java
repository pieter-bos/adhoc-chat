package transport_v2;

import java.io.IOException;

public class SynchronizationHandler implements PacketListener {
    private final SocketImpl socket;

    public SynchronizationHandler(SocketImpl socket) {
        this.socket = socket;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.isSyn() || !packet.getDestinationIp().equals(socket.getIp())) {
            return;
        }

        try {
            if(!packet.isAck()) {
                socket.sendAndAwaitAck(RawPacket.newSynAck(socket.newSequenceNumber(packet.getSourceIp()), packet.getSequenceNumber(), socket.getIp(), packet.getSourceIp()));
                // TODO add packet.getSourceIp() after acknowledged of SYN is received
            } else {
                socket.send(RawPacket.newAcknowledgement(packet.getSequenceNumber(), socket.getIp(), packet.getSourceIp()));
                socket.addToNetwork(packet.getSourceIp());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}