package client;

import client.protocol.*;
import client.wsjsonrpc.Expose;
import client.wsjsonrpc.WebSocketJsonRpcHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * ClientHandler handles traffic between the browser and between the client.
 */
public class ClientHandler implements WebSocketJsonRpcHandler {
    private NetworkHandler networkHandler;
    private String nick = null;

    /**
     * Constructor
     * @param networkHandler Reference to the networkhandler
     */
    public ClientHandler(NetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }

    @Expose
    public boolean nick(String nick) {
        this.nick = nick;
        System.out.println(nick);
        networkHandler.broadcast(serialize(new NickChangeMessange(nick)));
        return true;
    }

    @Expose boolean invite(int conversation, String destination) {
        networkHandler.send(
                serialize(new InviteMessage(conversation, null)),
                networkHandler.nameAddressMap.get(destination)
        );
        return true;
    }

    @Expose
    public boolean send(int conversation, String message, String destination) {
        networkHandler.send(
                serialize(new TextMessage(conversation, message)),
                networkHandler.nameAddressMap.get(destination)
        );
        return true;
    }

    @Expose
    public boolean leave(int conversation, String destination) {
        networkHandler.send(
                serialize(new LeaveMessage(conversation)),
                networkHandler.nameAddressMap.get(destination)
        );
        return true;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    /**
     * Serializes a message to a byte array
     * @param message Message
     * @return Byte array
     */
    private byte[] serialize(Message message) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(message);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }
}