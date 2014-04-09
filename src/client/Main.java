package client;

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
        server = new SimpleServer(8080);

        client = new ClientHandler(new InetSocketAddress(8081), network);
        client.start();

        network = new NetworkHandler(5000, client);
        network.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
