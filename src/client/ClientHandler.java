package client;

import client.wsjsonrpc.Expose;
import client.wsjsonrpc.WebSocketJsonRpcHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 * ClientHandler handles traffic between the browser and between the client.
 */
public class ClientHandler implements WebSocketJsonRpcHandler {
    private NetworkHandler networkHandler;
    private String nick = null;

    /**
     * Constructor
     * @param networkHandler
     */
    public ClientHandler(NetworkHandler networkHandler) {
        this.networkHandler = networkHandler;
    }

    @Expose
    public boolean nick(String nick) {
        this.nick = nick;
        System.out.println(nick);
        networkHandler.broadcastNickChange(nick);
        return true;
    }

    @Expose boolean invite(int conversation, String destination) {
        networkHandler.sendInviteMessage(conversation, destination);

        return true;
    }

    @Expose
    public boolean send(int conversation, String message, String destination) {
        networkHandler.sendTextMessage(conversation, message, destination);
        return true;
    }

    @Expose
    public boolean leave(int conversation, String destination) {
        networkHandler.sendLeaveMessage(conversation, destination);
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
}