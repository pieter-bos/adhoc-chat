package client;

import client.protocol.LeaveMessage;
import client.protocol.NickChangeMessage;
import client.protocol.TextMessage;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Keeps track of the complete state of the client
 */
public class ApplicationState {
    private ClientHandler client;
    private NetworkHandler network;

    private String nickname;
    private ClientAddressMapper users;
    private Map<Integer, Conversation> conversationList;

    public String getNickname() {
        return nickname;
    }

    public boolean setNickname(String nickname) {
        if (!users.contains(nickname)) {
            this.nickname = nickname;
            network.broadcast(new Gson().toJson(new NickChangeMessage(nickname)).getBytes());
            return true;
        }

        return false;
    }

    /**
     * Constructor
     */
    public ApplicationState() {
        users = new ClientAddressMapper();
        conversationList = new HashMap<>();
        int id = new Random().nextInt(10000);
        conversationList.put(id, new Conversation("", id));
    }

    public void setHandlers(ClientHandler client, NetworkHandler network) {
        this.client = client;
        this.network = network;
    }

    public void sendMessage(TextMessage message) {
        Conversation conv = this.conversationList.get(message.getConvId());
        InetAddress dest = this.users.get(conv.getUser());
        if (dest == null) {
            network.broadcast(new Gson().toJson(message).getBytes());
        }
    }

    public void addUser(NickChangeMessage message, InetAddress source) {
        users.put(message.getUsername(), source);
        client.newUser(message);
    }

    public void receiveMessage(TextMessage message, InetAddress source) {
        String user = users.get(source);
        Conversation conv = conversationList.get(message.getConvId());

        if (conv == null) {
            Conversation newConv = new Conversation(message.getNickname(), message.getConvId());
            conversationList.put(message.getConvId(), newConv);
            client.newConversation(newConv);
            conv = conversationList.get(message.getConvId());
        }

        conv.addMessage(new TextMessage(message.getMessage(), user, conv.getId()));
    }

    public Map<Integer, Conversation> getConversationList() {
        return conversationList;
    }

    public ClientAddressMapper getUsers() {
        return users;
    }

    public void clientLeft() {
        System.out.println("Client left");
        network.broadcast(new Gson().toJson(new LeaveMessage(nickname)).getBytes());
    }

    public void userLeft(LeaveMessage message) {
        client.removeUser(message);

        for (Conversation conv : conversationList.values()) {
            if (conv.getUser().equals(message.getUsername())) {
                conversationList.remove(conv.getId());
            }
        }
    }
}
