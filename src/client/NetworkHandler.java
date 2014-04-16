package client;

import client.protocol.*;
import com.google.gson.Gson;
import transport_v2.*;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread implements Observer {
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

        socket.addObserver(this);
    }

    @Override
    public void run() {
        while (listening) {
            Packet packet = null;
            try {
                packet = socket.receive();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            Message message = new Gson().fromJson(new String(packet.getData()), Message.class);

            String packetData = new String(packet.getData());
            switch (message.getType()) {
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
                    state.invite(inviteMessage);
                    break;
                case RequestNickMessage.TYPE:
                    RequestNickMessage requestNickMessage = new Gson().fromJson(packetData, RequestNickMessage.class);
                    state.requestNick(requestNickMessage, packet.getSourceAddress());
                    break;
                case LeaveConversationMessage.TYPE:
                    LeaveConversationMessage leaveConversationMessage = new Gson().fromJson(packetData, LeaveConversationMessage.class);
                    state.leaveConversation(leaveConversationMessage);
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

    @Override
    public void update(Observable o, Object arg) {
        for (InetAddress client : socket.getOtherClients()) {
            if (!state.getUsers().contains(client)) {
                broadcast(new Gson().toJson(new RequestNickMessage()).getBytes());
                break;
            }
        }

        for (String user : state.getUsers().values()) {
            if (!socket.getOtherClients().contains(state.getUsers().get(user))) {
                state.getUsers().remove(user);

                for (Conversation conv : state.getConversationList().values()) {
                    if (conv.getUser().equals(user)) {
                        state.getConversationList().remove(conv);
                    }
                }
            }
        }
    }
}
