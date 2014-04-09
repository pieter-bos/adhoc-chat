package client.wsjsonrpc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;

public class WebSocketJsonRpc<T extends WebSocketJsonRpcHandler> extends WebSocketServer {
    public static final int JSONRPC_PARSE_ERROR = -32700;
    public static final int JSONRPC_INVALID_REQUEST = -32600;
    public static final int JSONRPC_METHOD_NOT_FOUND = -32601;
    public static final int JSONRPC_INVALID_PARAMS = -32602;
    public static final int JSONRPC_INTERNAL_ERROR = -32603;

    private T handler;
    private Class<T> methodClass;
    private HashMap<WebSocket, HashSet<String>> subscriptions = new HashMap<>();

    public WebSocketJsonRpc(InetSocketAddress address, T handler, Class<T> cls) {
        super(address);
        this.handler = handler;
        this.methodClass = cls;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        subscriptions.put(webSocket, new HashSet<String>());
        handler.onOpen(webSocket, clientHandshake);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        handler.onClose(webSocket, i, s, b);
    }

    private void sendError(WebSocket webSocket, JsonRpcError error) {
        String data = new Gson().toJson(error);
        webSocket.send(data);
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        JsonObject element = new JsonParser().parse(s).getAsJsonObject();
        sendError(webSocket, new JsonRpcError(element.getAsJsonObject().get("id").getAsInt() + "", JSONRPC_METHOD_NOT_FOUND, "Method not found"));
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        handler.onError(webSocket, e);
    }
}
