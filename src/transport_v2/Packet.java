package transport_v2;

import java.net.InetAddress;

public interface Packet {
    // public Packet(byte[] data);

    /**
     * The source IP of the packet.
     * @return the source
     */
    public InetAddress getSourceAddress();

    /**
     * The destination IP.
     * @return the destination
     */
    public InetAddress getDestinationAddress();

    /**
     * Byte array with data.
     * @return the data
     */
    public byte[] getData();

    /**
     * The number of data bytes.
     * @return the length of the data
     */
    public int length();
}
