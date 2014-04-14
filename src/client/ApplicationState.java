package client;

import client.protocol.TextMessage;
import com.google.gson.Gson;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
//        network.broadcast(nickname.getBytes());
    }

    /**
     * Constructor
     * @param client
     * @param network
     */
    public ApplicationState() {
        users = new ClientAddressMapper();
        conversationList = new HashMap<>();
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
}
