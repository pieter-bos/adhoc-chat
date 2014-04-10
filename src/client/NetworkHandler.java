package client;

import client.protocol.*;
import transport.Socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread {
    private ClientHandler client;
    private Socket socket;
    private boolean listening = true;
    private HashMap<String, InetAddress> nameAddressMap;

    /**
     * Constructor of NetworkHandler
     * @param port
     * @param client
     */
    public NetworkHandler(int port, ClientHandler client){
        this.client = client;
        this.nameAddressMap = new HashMap<>();
    }

    @Override
    public void run() {
        //TODO implement
    }

    /**
     * Sends a message to the destination that this person has left the conversation
     * @param conversation ID of the conversation
     * @param destination Name of the receiver
     */
    public void sendLeaveMessage(int conversation, String destination) {
        socket.send(serialize(new LeaveMessage(conversation)), nameAddressMap.get(destination));
    }

    /**
     * Broadcasts the new nickname of this client over the network
     * @param nick Nickname of the client
     */
    public void broadcastNickChange(String nick) {
        socket.broadcast(serialize(new NickChangeMessange(nick)));
    }

    /**
     * Sends a text message to the destination
     * @param conversation
     * @param message
     */
    public void sendTextMessage(int conversation, String message, String destination) {
        socket.send(serialize(new TextMessage(conversation, message)), nameAddressMap.get(destination));
    }

    /**
     * Sends an invite for a conversation to the destination
     * @param conversation ID of the conversation
     * @param destination Name of the receiver
     */
    public void sendInviteMessage(int conversation, String destination) {
        socket.send(serialize(new InviteMessage(conversation, null)), nameAddressMap.get(destination));
    }

    /**
     * Serializes a message to a byte array
     * @param message Message
     * @return Byte array
     */
    private byte[] serialize(Message message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(message);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }
}
