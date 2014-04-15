package client;

import client.protocol.*;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Keeps track of the complete state of the client
 */
public class ApplicationState {
    private static final int MAX_CONVERSATION_ID = 100000;
    private ClientHandler client;
    private NetworkHandler network;

    private String nickname;
    private ClientAddressMapper users;
    private Map<Integer, Conversation> conversationList;

    private final Random random = new Random();

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
        int id = -1;
        conversationList.put(id, new Conversation("", id));
    }

    public void setHandlers(ClientHandler client, NetworkHandler network) {
        this.client = client;
        this.network = network;

        network.broadcast(new Gson().toJson(new RequestNickMessage()).getBytes());
    }

    public void sendMessage(TextMessage message) {
        Conversation conv = this.conversationList.get(message.getConvId());
        InetAddress dest = this.users.get(conv.getUser());
        if (dest == null) {
            network.broadcast(new Gson().toJson(message).getBytes());
        } else {
            network.send(new Gson().toJson(message).getBytes(), dest);
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

        client.newMessage(conv, message.getNickname(), message.getMessage());
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

    public int addConversation(String user) {
        int id = random.nextInt(MAX_CONVERSATION_ID);
        conversationList.put(id, new Conversation(user, id));
        network.send(new Gson().toJson(new InviteMessage(id, getNickname())).getBytes(), users.get(user));
        return id;
    }

    public void invite(InviteMessage inviteMessage) {
        conversationList.put(inviteMessage.getConversation(), new Conversation(inviteMessage.getOther(), inviteMessage.getConversation()));
        client.invite(inviteMessage);
    }

    public void requestNick(RequestNickMessage requestNickMessage, InetAddress sourceAddress) {
        network.send(new Gson().toJson(new NickChangeMessage(getNickname())).getBytes(), sourceAddress);
    }
}
