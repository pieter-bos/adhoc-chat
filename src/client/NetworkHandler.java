package client;

import client.protocol.NetworkMessage;
import transport.Socket;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread {
    private ClientHandler client;
    private Socket socket;
    private boolean listening = true;

    /**
     * Constructor of NetworkHandler
     * @param port
     * @param client
     */
    public NetworkHandler(int port, ClientHandler client){
        this.client = client;
        //TODO: make socket
    }

    @Override
    public void run() {
    }

    /**
     * Writes the message to the network
     * @param message
     */
    public void write(NetworkMessage message) {
        socket.send(message.toByteArray(), null);
    }
}
