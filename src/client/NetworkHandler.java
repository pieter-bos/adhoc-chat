package client;

import client.protocol.InviteMessage;
import client.protocol.Message;
import client.protocol.NickChangeMessange;
import transport.Packet;
import transport.Socket;
import transport.SocketImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread {
    private ClientHandler client;
    private Socket socket;
    private boolean listening = true;
    public HashMap<String, InetAddress> nameAddressMap;

    /**
     * Constructor of NetworkHandler
     * @param port
     * @param client
     */
    public NetworkHandler(int port, ClientHandler client){
        this.client = client;
        this.nameAddressMap = new HashMap<>();
        try {
            this.socket = new SocketImpl(port);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (listening) {
            Packet packet = socket.receive();
            Message message = deserialize(packet.getData());

            if (message instanceof NickChangeMessange) {
                updateClientList(packet.getSourceAddress(), message);
            }
        }
    }

    private void updateClientList(InetAddress sourceAddress, Message message) {

    }

    /**
     * Wrapper for send method of socket
     * @param data Data array
     * @param dest Destination address
     */
    public void send(byte[] data, InetAddress dest) {
        socket.send(data, dest);
    }

    /**
     * Wrapper for broadcast method of socket
     * @param data Data array
     */
    public void broadcast(byte[] data) {
        socket.broadcast(data);
    }

    /**
     * Deserializes a message from a byte array
     * @param data Input array
     * @return Deserialized message
     */
    private Message deserialize(byte[] data) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream in = new ObjectInputStream(bis);
            Message message = (Message) in.readObject();
            return message;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
