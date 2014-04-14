package client;

import client.protocol.NickChangeMessage;
import client.protocol.TextMessage;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

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

    public void setNickname(String nickname) {
        this.nickname = nickname;
        network.broadcast(new Gson().toJson(new NickChangeMessage(nickname)).getBytes());
    }

    /**
     * Constructor
     */
    public ApplicationState() {
        users = new ClientAddressMapper();
        conversationList = new HashMap<>();
        int id = new Random().nextInt(10000);
        conversationList.put(id, new Conversation("", id));
        try {
            users.put("Sophie", InetAddress.getByName("192.168.2.1"));
            users.put("Laurens", InetAddress.getByName("192.168.2.2"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setHandlers(ClientHandler client, NetworkHandler network) {
        this.client = client;
        this.network = network;
    }

    public void sendMessage(TextMessage message) {
        Conversation conv = this.conversationList.get(message.getConvId());
        InetAddress dest = this.users.get(conv.getUser());
        network.send(new Gson().toJson(message).getBytes(), dest);
    }

    public void addUser(NickChangeMessage message, InetAddress source) {
        users.put(message.getNick(), source);
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
}
