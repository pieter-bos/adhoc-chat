package client;

import client.wsjsonrpc.WebSocketJsonRpc;

import java.net.InetSocketAddress;

/**
 * Entry point for the chat application
 */
public class Main {
    private ClientHandler client;
    private NetworkHandler network;
    private SimpleServer server;

    /**
     * Constructor
     */
    public Main() {
        WebSocketJsonRpc<ClientHandler> rpc = new WebSocketJsonRpc<ClientHandler>(new InetSocketAddress(8081), new ClientHandler(network), ClientHandler.class);
        rpc.start();

        network = new NetworkHandler(5000, client);
        network.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
