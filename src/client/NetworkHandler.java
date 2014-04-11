package client;

import client.protocol.InviteMessage;
import client.protocol.Message;
import client.protocol.NickChangeMessange;
import client.protocol.TextMessage;
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
    public ClientAddressMapper nameAddressMap;

    /**
     * Constructor of NetworkHandler
     * @param port
     * @param client
     */
    public NetworkHandler(int port, ClientHandler client){
        this.client = client;
        this.nameAddressMap = new ClientAddressMapper();
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
                updateClientList(packet.getSourceAddress(), (NickChangeMessange) message);
            } else if (message instanceof TextMessage) {
                sendTextMessage(packet.getSourceAddress(), (TextMessage) message);
            }
        }
    }

    private void sendTextMessage(final InetAddress sourceAddress, final TextMessage message) {
        client.sendToClient("textMessage", new Object() {
            private String source = nameAddressMap.get(sourceAddress);
            private Message content = message;
        });
    }

    private void updateClientList(final InetAddress sourceAddress, final NickChangeMessange message) {
        nameAddressMap.put(message.getNick(), sourceAddress);

        client.sendToClient("nickChange", new Object() {
            private String source = nameAddressMap.get(sourceAddress);
            private Message content = message;
        });
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
