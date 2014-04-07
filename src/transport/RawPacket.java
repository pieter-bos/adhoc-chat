package transport;

public class RawPacket {
    private byte flags;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private byte[] data;

    public RawPacket(byte[] packet) {

    }

}
