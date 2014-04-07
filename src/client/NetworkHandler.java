package client;

import client.protocol.Message;
import transport.Socket;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread {
    private ClientHandler clientHandler = null;
    private Socket socket = null;
    private boolean isListening = true;

    /**
     * Constructor of NetworkHandler
     * @param port
     * @param clientHandler
     */
    public void NetworkHandler(int port, ClientHandler clientHandler){
        this.clientHandler = clientHandler;
        //TODO: make socket

    }

    @Override
    public void run() {

        while (this.isListening) {
            // Handel berichten af
        }
    }

    /**
     * Writes the message to the network
     * @param message
     */
    public void write(Message message) {
        socket.send(message.toByteArray(), null);
    }
}
