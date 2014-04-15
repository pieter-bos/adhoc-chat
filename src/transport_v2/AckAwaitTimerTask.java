package transport_v2;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimerTask;

public class AckAwaitTimerTask extends TimerTask {
    private final SocketImpl socket;
    private final RawPacket packet;
    private final HashMap<InetAddress, HashSet<Integer>> sentButNoAck;
    private final int retries;

    public AckAwaitTimerTask(SocketImpl socket, RawPacket packet, HashMap<InetAddress, HashSet<Integer>> sentButNoAck, int retries) {
        this.socket = socket;
        this.packet = packet;
        this.sentButNoAck = sentButNoAck;
        this.retries = retries;
    }

    @Override
    public void run() {
        boolean acked;

        synchronized(sentButNoAck) {
            if(sentButNoAck.containsKey(packet.getDestinationIp())) {
                acked = !sentButNoAck.get(packet.getDestinationIp()).contains(packet.getSequenceNumber());
            } else {
                acked = true;
            }
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