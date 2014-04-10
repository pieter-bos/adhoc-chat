package client;

import client.protocol.*;
import transport.Socket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread {
    private ClientHandler client;
    private Socket socket;
    private boolean listening = true;
    public HashMap<String, InetAddress> nameAddressMap;

    /**
     * Constructor of NetworkHandler
     * @param port
     * @param client
     */
    public NetworkHandler(int port, ClientHandler client){
        this.client = client;
        this.nameAddressMap = new HashMap<>();
    }

    @Override
    public void run() {
        //TODO implement
    }

    /**
     * Wrapper for send method of socket
     * @param data Data array
     * @param dest Destination address
     */
    public void send(byte[] data, InetAddress dest) {
        socket.send(data, dest);
    }

    /**
     * Wrapper for broadcast method of socket
     * @param data Data array
     */
    public void broadcast(byte[] data) {
        socket.broadcast(data);
    }
}
