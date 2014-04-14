package transport_v2;

import java.io.IOException;
import java.util.HashSet;
import java.util.TimerTask;

public class AckAwaitTimerTask extends TimerTask {
    private SocketImpl socket;
    private RawPacket packet;
    private HashSet<RawPacket> sentButNoAck;
    private int retries;

    public AckAwaitTimerTask(SocketImpl socket, RawPacket packet, HashSet<RawPacket> sentButNoAck, int retries) {
        this.socket = socket;
        this.packet = packet;
        this.sentButNoAck = sentButNoAck;
        this.retries = retries;
    }

    @Override
    public void run() {
        boolean acked;

        synchronized(sentButNoAck) {
            acked = sentButNoAck.contains(packet);
        }

        if(!acked) {
            RawPacket newPacket = packet.duplicate();

            try {
                socket.sendAndRetry(newPacket, retries - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}