package client;

import client.wsjsonrpc.WebSocketJsonRpc;
import client.wsjsonrpc.WebSocketJsonRpcHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 * ClientHandler handles traffic between the browser and between the client.
 */
public class ClientHandler implements WebSocketJsonRpcHandler {
    private ApplicationState state;
    private WebSocketJsonRpc<ClientHandler> rpc;

    public ClientHandler(ApplicationState state, WebSocketJsonRpc<ClientHandler> rpc) {
        this.state = state;
        this.rpc = rpc;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
    //TODO implement
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
    //TODO implement
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
    //TODO implement
    }
}