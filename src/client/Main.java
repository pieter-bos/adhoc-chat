package client;

import java.net.InetSocketAddress;

/**
 * Entry point for the chat application
 */
public class Main {
    public static void main(String[] args) {
        SimpleServer server = new SimpleServer(8080);
        ClientHandler clientHandler = new ClientHandler(new InetSocketAddress(8081),null);
        clientHandler.start();
    }
}
