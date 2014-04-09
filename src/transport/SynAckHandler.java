package transport;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SynAckHandler implements PacketListener {
    private ReentrantLock lock;
    private final Condition condition;
    private final HashSet<InetAddress> candidates;
    private final HashSet<InetAddress> verified;

    public SynAckHandler(ReentrantLock lock, Condition condition, HashSet<InetAddress> candidates, HashSet<InetAddress> verified) {
        this.lock = lock;
        this.condition = condition;
        this.candidates = candidates;
        this.verified = verified;
    }

    @Override
    public void onPacketReceived(RawPacket packet) {
        lock.lock();

        try {
            if(packet.isSyn() && packet.isAck()) {
                candidates.add(packet.getSourceIp());
                verified.add(packet.getSourceIp());

                ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
                byte[] ip = new byte[4];

                while(buffer.remaining() > 0) {
                    buffer.get(ip);
                    try {
                        candidates.add(InetAddress.getByAddress(ip));
                    } catch (UnknownHostException e) {  }
                }
            }

            if(verified.size() > 0 && candidates.equals(verified)) {
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}
