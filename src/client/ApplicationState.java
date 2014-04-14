package client;

import java.util.LinkedList;
import java.util.List;

/**
 * Keeps track of the complete state of the client
 */
public class ApplicationState {
    private String nickname;
    private ClientAddressMapper users;
    private List<Conversation> conversationList;

    /**
     * Constructor
     * @param address ip address of the client
     */
    public ApplicationState() {
        users = new ClientAddressMapper();
        conversationList = new LinkedList<>();
    }
}
