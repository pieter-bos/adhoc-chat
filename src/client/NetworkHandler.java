package client;

import client.protocol.*;
import com.google.gson.Gson;
import transport.Packet;
import transport.Socket;
import transport.SocketImpl;

import java.io.IOException;
import java.net.InetAddress;

/**
 * NetworkHandler handles all network traffic.
 */
public class NetworkHandler extends Thread {
    private ApplicationState state;
    private Socket socket;
    private boolean listening = true;
    public ClientAddressMapper nameAddressMap;

    /**
     * Constructor of NetworkHandler
     * @param port
     */
    public NetworkHandler(int port, ApplicationState state){
        this.state = state;
        try {
            this.socket = new SocketImpl(port);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (listening) {
            Packet packet = socket.receive();
            client.protocol.Message message = new Gson().fromJson(new String(packet.getData()), client.protocol.Message.class);
            if (message instanceof NickChangeMessage) {
                state.addUser((NickChangeMessage) message, packet.getSourceAddress());
            } else if (message instanceof TextMessage) {
                state.receiveMessage((TextMessage) message, packet.getSourceAddress());
            }
        }
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
