package transport;

import java.net.InetAddress;

public class PacketImpl implements Packet {
    private byte[] data;
    private InetAddress source;
    private InetAddress destination;

    public PacketImpl(byte[] data, InetAddress source, InetAddress destination) {
        this.data = data;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public InetAddress getSourceAddress() {
        return source;
    }

    @Override
    public InetAddress getDestinationAddress() {
        return destination;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int length() {
        return data.length;
    }
}
