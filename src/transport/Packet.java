package transport;

import java.net.InetAddress;

public interface Packet {
    // public Packet(byte[] data);

    public InetAddress getSourceAddress();

    public InetAddress getDestinationAddress();

    public byte[] getData();

    public int length();
}
