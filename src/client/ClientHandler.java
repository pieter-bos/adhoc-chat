package client;

import client.protocol.Message;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * ClientHandler handles traffic between the browser and between the client.
 */
public class ClientHandler extends WebSocketServer {
    private NetworkHandler networkHandler;

    /**
     * Constructor
     *
     * @param address
     * @param networkHandler
     */
    public ClientHandler(InetSocketAddress address, NetworkHandler networkHandler) {
        super(address);
        this.networkHandler = networkHandler;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
//        broadcast ("we zijn er");

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        // Broadcast ("we zijn er niet meer")

    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        //Build byte message

        // Send to "de netwerk"

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        // Broadcast ("we zijn er niet meer")
    }

    /**
     * Writes the message to the webSocket
     * @param message
     */
    public void write(Message message) {
        sendToAll(message.toJSON());
    }

    /**
     * Sends the text to all connected clients
     * @param text
     */
    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}