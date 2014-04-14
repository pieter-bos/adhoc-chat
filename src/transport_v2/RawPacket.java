package transport_v2;

import exceptions.InvalidPacketException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class RawPacket {
    public static final int MIN_SIZE = 21;

    public static final byte ACK_MASK =      0b00000001;
    public static final byte SYN_MASK =      0b00000010;
    public static final byte ANNOUNCE_MASK = 0b00000100;
// private static final byte ZERO_MASK = 0b10000000; this bit must always be zero because evil stupid java only has signed byte.

    private int nonce;
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
            nonce = bb.getInt();
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

            data = new byte[bb.remaining()];
            while (bb.remaining() > 0) {
                data[data.length - bb.remaining()] = bb.get();
            }
        }
    }

    public RawPacket(int nonce, byte flags, int sequenceNumber, int acknowledgmentNumber,
                     InetAddress sourceAddress, InetAddress destinationAddress) throws InvalidPacketException {
        this(nonce, flags, sequenceNumber, acknowledgmentNumber,
                sourceAddress.getAddress(), destinationAddress.getAddress(), null);
    }

    public RawPacket(int nonce, byte flags, int sequenceNumber, int acknowledgmentNumber,
                     InetAddress sourceAddress, InetAddress destinationAddress,
                     byte[] data) throws InvalidPacketException {
        this(nonce, flags, sequenceNumber, acknowledgmentNumber,
                sourceAddress.getAddress(), destinationAddress.getAddress(), data);
    }

    public RawPacket(int nonce, byte flags, int sequenceNumber, int acknowledgmentNumber,
                     byte[] sourceAddress, byte[] destinationAddress) throws InvalidPacketException {
        this(nonce, flags, sequenceNumber, acknowledgmentNumber, sourceAddress, destinationAddress, null);
    }

    public RawPacket(int nonce, byte flags, int sequenceNumber, int acknowledgmentNumber,
                     byte[] sourceAddress, byte[] destinationAddress, byte[] data) throws InvalidPacketException {
        if (flags < 0) {
            throw new InvalidPacketException(":(");
        } else if (sourceAddress.length != 4 || destinationAddress.length != 4) {
            throw new InvalidPacketException("Source and destination address need to be 4 bytes.");
        } else {
            this.nonce = nonce;
            this.flags = flags;
            this.sequenceNumber = sequenceNumber;
            this.acknowledgmentNumber = acknowledgmentNumber;
            this.sourceAddress = sourceAddress.clone();
            this.destinationAddress = destinationAddress.clone();
            if (data != null) {
                this.data = data.clone();
            } else {
                this.data = new byte[] {};
            }
        }
    }

    public boolean isAck() {
        return (flags & ACK_MASK) > 0;
    }

    public boolean isSyn() {
        return (flags & SYN_MASK) > 0;
    }

    public boolean isAnnounce() {
        return (flags & ANNOUNCE_MASK) > 0;
    }

    public int getNonce() {
        return nonce;
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

        buffer.putInt(nonce);
        buffer.put(flags);
        buffer.putInt(sequenceNumber);
        buffer.putInt(acknowledgmentNumber);
        buffer.put(sourceAddress);
        buffer.put(destinationAddress);
        buffer.put(data);

        return buffer.array();
    }

    /**
     * The number of byte of the entire packet.
     * @return the length of the packet
     */
    public int getLength() {
        return MIN_SIZE + data.length;
    }

    @Override
    public int hashCode() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(nonce);
        buffer.put(sourceAddress);
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

        result += isAnnounce() ? "ANN" : "";
        result += isSyn() ? "SYN" : "";
        result += isAck() ? "ACK" : "";

        result += (!result.equals("")) ? ": " : "";

        result += "nonce=" + getNonce();
        result += ", seq=" + getSequenceNumber();
        result += ", ack=" + getAcknowledgmentNumber();
        result += ", len=" + data.length;

        result += ", src=" + getSourceIp();
        result += ", dst=" + getDestinationIp();

        return result;
    }

////// static methods for creating specific packets

}
