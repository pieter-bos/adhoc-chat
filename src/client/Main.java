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
    private SimpleServer server;

    /**
     * Constructor
     */
    public Main() {
        state = new ApplicationState();
        network = new NetworkHandler(3000, state);
        client = new ClientHandler(state);
        state.setHandlers(client, network);
        final WebSocketJsonRpc<ClientHandler> rpc = new WebSocketJsonRpc<ClientHandler>(new InetSocketAddress(8081), client, ClientHandler.class);
        client.setSocket(rpc);
        server = new SimpleServer(8080);

        rpc.start();
        network.start();

    }

    public static void main(String[] args) {
        new Main();
    }
}
