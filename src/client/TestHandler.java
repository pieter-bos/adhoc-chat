package client;

import client.wsjsonrpc.WebSocketJsonRpcHandler;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public class TestHandler implements WebSocketJsonRpcHandler {
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
