package transport_v2;

import java.io.IOException;

public class BroadcastHandler implements PacketListener {
    private SocketImpl socket;

    public BroadcastHandler(SocketImpl socket) {
        this.socket = socket;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        if(!packet.getDestinationIp().equals(socket.getIp())) {
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
