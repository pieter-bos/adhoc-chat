package client.wsjsonrpc;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

public interface WebSocketJsonRpcHandler {
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake);
    public void onClose(WebSocket webSocket, int i, String s, boolean b);
    public void onError(WebSocket webSocket, Exception e);
}
