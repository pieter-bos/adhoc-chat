package transport;

import java.net.InetAddress;

public class PacketImpl implements Packet {
    private byte[] data;

    public PacketImpl(byte[] data) {
        this.data = data;
    }

    @Override
    public InetAddress getSourceAddress() {
        return null;
    }

    @Override
    public InetAddress getDestinationAddress() {
        return null;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int length() {
        return data.length;
    }
}
