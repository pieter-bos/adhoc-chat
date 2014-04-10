package transport;

import exceptions.InvalidPacketException;

import java.net.InetAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class RawPacket {
    public static final int MIN_SIZE = 21;

    public static final byte SYN_MASK =      0b00000001;
    public static final byte ACK_MASK =      0b00000010;
    public static final byte MAGIC_MASK =    0b00000100;
    public static final byte EVIL_MASK =     0b00001000;
    public static final byte RESERVED_MASK = 0b00100000;
// private static final byte ZERO_MASK = 0b10000000; this bit must always be zero because evil stupid java only has signed byte.

    private byte flags;
    private int sequenceNumber;
    private int acknowledgmentNumber;
    private int retransmissionNumber;
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
            retransmissionNumber = bb.getInt();

            for (int i = 0; i < 4; i++) {
                sourceAddress[i] = bb.get();
            }
            for (int i = 0; i < 4; i++) {
                destinationAddress[i] = bb.get();
            }

            data = bb.slice().array();
        }
    }

    public RawPacket(byte flags, int sequenceNumber, int acknowledgmentNumber, int retransmissionNumber,
                     InetAddress sourceAddress, InetAddress destinationAddress) throws InvalidPacketException {
        this(flags, sequenceNumber, acknowledgmentNumber, retransmissionNumber,
                sourceAddress.getAddress(), destinationAddress.getAddress(), null);
    }

    public RawPacket(byte flags, int sequenceNumber, int acknowledgmentNumber, int retransmissionNumber,
                     InetAddress sourceAddress, InetAddress destinationAddress,
                     byte[] data) throws InvalidPacketException {
        this(flags, sequenceNumber, acknowledgmentNumber, retransmissionNumber,
                sourceAddress.getAddress(), destinationAddress.getAddress(), data);
    }

    public RawPacket(byte flags, int sequenceNumber, int acknowledgmentNumber, int retransmissionNumber,
                     byte[] sourceAddress, byte[] destinationAddress) throws InvalidPacketException {
        this(flags, sequenceNumber, acknowledgmentNumber, retransmissionNumber, sourceAddress, destinationAddress, null);
    }

    public RawPacket(byte flags, int sequenceNumber, int acknowledgmentNumber, int retransmissionNumber,
                     byte[] sourceAddress, byte[] destinationAddress, byte[] data) throws InvalidPacketException {
        if (flags < 0) {
            throw new InvalidPacketException(":(");
        } else if (sourceAddress.length != 4 || destinationAddress.length != 4) {
            throw new InvalidPacketException("Source and destination address need to be 4 bytes.");
        } else {
            this.flags = flags;
            this.sequenceNumber = sequenceNumber;
            this.acknowledgmentNumber = acknowledgmentNumber;
            this.retransmissionNumber = retransmissionNumber;
            this.sourceAddress = sourceAddress.clone();
            this.destinationAddress = destinationAddress.clone();
            if (data != null) {
                this.data = data.clone();
            } else {
                this.data = new byte[] {};
            }
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

    public int getRetransmissionNumber() {
        return retransmissionNumber;
    }

    public byte[] getSourceAddress() {
        return sourceAddress;
    }

    public InetAddress getSourceIp() {
        try {
            return InetAddress.getByAddress(getSourceAddress());
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public byte[] getDestinationAddress() {
        return destinationAddress;
    }

    public InetAddress getDestinationIp() {
        try {
            return InetAddress.getByAddress(getDestinationAddress());
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getLength());

        buffer.put(flags);
        buffer.putInt(sequenceNumber);
        buffer.putInt(acknowledgmentNumber);
        buffer.putInt(retransmissionNumber);
        buffer.put(sourceAddress);
        buffer.put(destinationAddress);
        buffer.put(data);

        return buffer.array();
    }

    public int getLength() {
        return MIN_SIZE + data.length;
    }

    @Override
    public int hashCode() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putInt(sequenceNumber);
        buffer.putInt(retransmissionNumber);
        buffer.put(sourceAddress);
        buffer.put(destinationAddress);
        buffer.rewind();
        return buffer.hashCode();
    }

    public static RawPacket tryParse(byte[] packet) {
        RawPacket result = null;

        try {
            result =  new RawPacket(packet);
        } catch (InvalidPacketException e) { }

        return result;
    }

    public byte getFlags() {
        return flags;
    }

    @Override
    public String toString() {
        String result = "";

        result += isSyn() ? "SYN" : "";
        result += isAck() ? "ACK" : "";

        result += (!result.equals("")) ? ": " : "";

        result += "seq=" + getSequenceNumber();
        result += ", ack=" + getAcknowledgmentNumber();
        result += ", rtns=" + getRetransmissionNumber();
        result += ", len=" + data.length;

        return result;
    }
}
