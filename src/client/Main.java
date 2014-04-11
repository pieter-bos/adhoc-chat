package client;

import client.wsjsonrpc.WebSocketJsonRpc;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;

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
        final WebSocketJsonRpc<ClientHandler> rpc = new WebSocketJsonRpc<ClientHandler>(new InetSocketAddress(8081), new ClientHandler(network), ClientHandler.class);
        rpc.start();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                rpc.notify("testStream", 1, 2, 3);
            }
        }, 0, 1000);

//        network = new NetworkHandler(5000, client);
//        network.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
