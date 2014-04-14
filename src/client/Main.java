package client;

import client.wsjsonrpc.WebSocketJsonRpc;
import java.net.InetSocketAddress;

/**
 * Entry point for the chat application
 */
public class Main {
    private ApplicationState state;
    private ClientHandler client = null;
    private NetworkHandler network;

    /**
     * Constructor
     */
    public Main() {
        network = new NetworkHandler(3000, state);
        client = new ClientHandler(state);
        final WebSocketJsonRpc<ClientHandler> rpc = new WebSocketJsonRpc<ClientHandler>(new InetSocketAddress(8081), client, ClientHandler.class);
        client.setSocket(rpc);

        rpc.start();
        state = new ApplicationState(client, network);
    }

    public static void main(String[] args) {
        new Main();
    }
}
