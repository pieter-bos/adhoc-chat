package transport;

import exceptions.InvalidPacketException;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class RawPacket {
    public static final int MIN_SIZE = 17;

    private static final byte SYN_MASK =      0b00000001;
    private static final byte ACK_MASK =      0b00000010;
    private static final byte MAGIC_MASK =    0b00000100;
    private static final byte EVIL_MASK =     0b00001000;
    private static final byte RESERVED_MASK = 0b00100000;
// private static final byte ZERO_MASK = 0b10000000; this bit must always be zero because evil stupid java only has signed byte.

    private byte flags;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private byte[] sourceAddress = new byte[4];
    private byte[] destinationAddress = new byte[4];
    private byte[] data;

    public RawPacket(byte[] packet) throws InvalidPacketException {
        if (packet.length < MIN_SIZE) {
            throw new InvalidPacketException("Packet too small.");
        } else {
            ByteBuffer bb = ByteBuffer.wrap(packet);
            flags = bb.get();
            if (flags < 0) {
                throw new InvalidPacketException(":(");
            }
            sequenceNumber = bb.getInt();
            acknowledgmentNumber = bb.getInt();

            for (int i = 0; i < 4; i++) {
                sourceAddress[i] = bb.get();
            }
            for (int i = 0; i < 4; i++) {
                destinationAddress[i] = bb.get();
            }

            data = bb.slice().array();
        }
    }

    public boolean isSyn() {
        return (flags & SYN_MASK) > 0;
    }

    public boolean isAck() {
        return (flags & ACK_MASK) > 0;
    }

    public boolean isMagic() {
        return (flags & MAGIC_MASK) > 0;
    }

    public boolean isEvil() {
        return (flags & EVIL_MASK) > 0;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getAcknowledgmentNumber() {
        return acknowledgmentNumber;
    }

    public byte[] getSourceAddress() {
        return sourceAddress;
    }

    public byte[] getDestinationAddress() {
        return destinationAddress;
    }

    public byte[] getData() {
        return data;
    }

    public static RawPacket tryParse(byte[] packet) {
        RawPacket result = null;
        try {
            result =  new RawPacket(packet);
        } catch (InvalidPacketException e) { }
        
        return result;
    }
}
