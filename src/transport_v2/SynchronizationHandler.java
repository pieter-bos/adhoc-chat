package transport_v2;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

public class SynchronizationHandler implements PacketListener {
    private final SocketImpl socket;
    private HashMap<InetAddress, HashSet<Integer>> sentSynAckButNoAck;

    public SynchronizationHandler(SocketImpl socket, HashMap<InetAddress, HashSet<Integer>> sentSynAckButNoAck) {
        this.socket = socket;
        this.sentSynAckButNoAck = sentSynAckButNoAck;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.isSyn() || !packet.getDestinationIp().equals(socket.getIp())) {
            return;
        }

        try {
            if(!packet.isAck()) {
                RawPacket synAck = RawPacket.newSynAck(socket.newSequenceNumber(packet.getSourceIp()), packet.getSequenceNumber(), socket.getIp(), packet.getSourceIp());
                socket.sendAndAwaitAck(synAck);

                synchronized(sentSynAckButNoAck) {
                    if(!sentSynAckButNoAck.containsKey(packet.getSourceIp())) {
                        sentSynAckButNoAck.put(packet.getSourceIp(), new HashSet<Integer>());
                    }

                    sentSynAckButNoAck.get(packet.getSourceIp()).add(synAck.getSequenceNumber());
                }
            } else {
                socket.send(RawPacket.newAcknowledgement(packet.getSequenceNumber(), socket.getIp(), packet.getSourceIp()));
                socket.addToNetwork(packet.getSourceIp());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}