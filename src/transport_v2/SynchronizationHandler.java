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

        socket.addToNetwork(packet.getSourceIp(), packet.getSequenceNumber());

        try {
            if(!packet.isAck()) {
                socket.sendAndAwaitAck(RawPacket.newSynAck(socket.newSequenceNumber(packet.getSourceIp()), packet.getSequenceNumber(), socket.getIp(), packet.getSourceIp()));
            } else {
                socket.send(RawPacket.newAcknowledgement(packet.getSequenceNumber(), socket.getIp(), packet.getSourceIp()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}