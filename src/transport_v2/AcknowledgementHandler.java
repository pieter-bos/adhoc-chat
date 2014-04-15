package transport_v2;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;

public class AcknowledgementHandler implements PacketListener {
    private final SocketImpl socket;
    private final HashMap<InetAddress, HashSet<Integer>> sentButNoAck;
    private final HashMap<InetAddress, HashSet<Integer>> sentSynAckButNoAck;

    public AcknowledgementHandler(SocketImpl socket, HashMap<InetAddress, HashSet<Integer>> sentButNoAck, HashMap<InetAddress, HashSet<Integer>> sentSynAckButNoAck) {
        this.socket = socket;
        this.sentButNoAck = sentButNoAck;
        this.sentSynAckButNoAck = sentSynAckButNoAck;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.getDestinationIp().equals(socket.getIp()) || !packet.isAck()) {
            return;
        }

        synchronized(sentSynAckButNoAck) {
            if(sentSynAckButNoAck.containsKey(packet.getSourceIp())) {
                if(sentSynAckButNoAck.get(packet.getSourceIp()).remove(packet.getAcknowledgmentNumber())) {
                    socket.addToNetwork(packet.getSourceIp());
                }
            }
        }

        synchronized(sentButNoAck) {
            if(sentButNoAck.containsKey(packet.getSourceIp())) {
                sentButNoAck.get(packet.getSourceIp()).remove(packet.getAcknowledgmentNumber());
            }
        }
    }
}
