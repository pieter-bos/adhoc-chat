package transport_v2;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class MockSocketImpl extends SocketImpl {
    public static final InetAddress IP = InetAddress.getLoopbackAddress();

    private Queue<Tuple<RawPacket, Integer>> messageQueue = new LinkedList<>();
    private HashSet<InetAddress> network = new HashSet<>();
    private int seq = 0;

    public MockSocketImpl() throws IOException {
        super(1888);
    }

    protected void sendAndRetry(RawPacket packet, int retries) throws IOException {
        messageQueue.add(new Tuple<RawPacket, Integer>(packet, retries));
    }

    public Queue<Tuple<RawPacket, Integer>> getMessageQueue() {
        return messageQueue;
    }

    @Override
    public InetAddress getIp() {
        return IP;
    }

    @Override
    public void addToNetwork(InetAddress other) {
        network.add(other);
    }

    @Override
    public int newSequenceNumber(InetAddress other) {
        return seq++;
    }

    public HashSet<InetAddress> getNetwork() {
        return network;
    }
}