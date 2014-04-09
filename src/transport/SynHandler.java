package transport;

import exceptions.InvalidPacketException;

import java.net.InetAddress;
import java.util.HashSet;

public class SynHandler implements PacketListener {
    private final SocketImpl socket;
    private final HashSet<InetAddress> network;

    public SynHandler(SocketImpl socket, HashSet<InetAddress> network) {
        this.socket = socket;
        this.network = network;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(packet.isSyn() && !packet.isAck()) {
            socket.makeSequenceNumber(packet.getSourceIp());
            socket.gotSequenceNumber(packet.getSourceIp(), packet.getSequenceNumber());

            synchronized (network) {
                network.add(packet.getSourceIp());
            }

            try {
                socket.send(new RawPacket((byte)(RawPacket.SYN_MASK | RawPacket.ACK_MASK),
                        socket.getSequenceNumber(packet.getSourceIp()),
                        packet.getSequenceNumber(), 0,
                        socket.getAddress(), packet.getSourceIp()));
            } catch (InvalidPacketException e) {  }
        }
    }
}
