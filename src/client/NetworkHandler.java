package client;

import client.protocol.*;
import client.protocol.Message;
import com.google.gson.Gson;
import transport_v2.Packet;
import transport_v2.Socket;
import transport_v2.SocketImpl;

import java.io.IOException;
import java.net.InetAddress;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread {
    private ApplicationState state;
    private Socket socket;
    private boolean listening = true;
    public ClientAddressMapper nameAddressMap;

    /**
     * Constructor of NetworkHandler
     * @param port
     */
    public NetworkHandler(int port, ApplicationState state){
        this.state = state;
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
            Packet packet = null;
            try {
                packet = socket.receive();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String packetData = new String(packet.getData());
            Message message = new Gson().fromJson(packetData, Message.class);

            switch(message.getType()) {
                case NickChangeMessage.TYPE:
                    NickChangeMessage nickChangeMessage = new Gson().fromJson(packetData, NickChangeMessage.class);
                    state.addUser(nickChangeMessage, packet.getSourceAddress());
                    break;
                case TextMessage.TYPE:
                    TextMessage textMessage = new Gson().fromJson(packetData, TextMessage.class);
                    state.receiveMessage(textMessage, packet.getSourceAddress());
                    break;
                case LeaveMessage.TYPE:
                    LeaveMessage leaveMessage = new Gson().fromJson(packetData, LeaveMessage.class);
                    state.userLeft(leaveMessage);
                    break;
                case InviteMessage.TYPE:
                    InviteMessage inviteMessage = new Gson().fromJson(packetData, InviteMessage.class);
                    // TODO implement
                    break;
            }
        }
    }

    /**
     * Wrapper for send method of socket
     * @param data Data array
     * @param dest Destination address
     */
    public void send(byte[] data, InetAddress dest) {
        try {
            socket.send(data, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wrapper for broadcast method of socket
     * @param data Data array
     */
    public void broadcast(byte[] data) {
        try {
            socket.broadcast(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
