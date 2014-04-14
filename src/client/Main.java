package client;

import client.wsjsonrpc.WebSocketJsonRpc;
import java.net.InetSocketAddress;

/**
 * Entry point for the chat application
 */
public class Main {
    private ApplicationState state;
    private ClientHandler client;
    private NetworkHandler network;

    /**
     * Constructor
     */
    public Main() {
        final WebSocketJsonRpc<ClientHandler> rpc = new WebSocketJsonRpc<ClientHandler>(new InetSocketAddress(8081), client, ClientHandler.class);
        network = new NetworkHandler(3000, state);
        client = new ClientHandler(state, rpc);
        state = new ApplicationState();

        rpc.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
