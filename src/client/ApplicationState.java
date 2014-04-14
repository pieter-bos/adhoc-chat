package client;

import client.protocol.TextMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps track of the complete state of the client
 */
public class ApplicationState {
    private ClientHandler client;
    private NetworkHandler network;

    private String nickname;
    private ClientAddressMapper users;
    private List<Conversation> conversationList;

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
        conversationList = new LinkedList<>();
    }

    public void setHandlers(ClientHandler client, NetworkHandler network) {
        this.client = client;
        this.network = network;
    }

    public void sendMessage(TextMessage message) {
//        network.broadcast(message);
    }
}
