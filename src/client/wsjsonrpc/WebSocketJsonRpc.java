package client.wsjsonrpc;

import com.google.gson.*;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

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

    private JsonRpcResponse getResponse(JsonObject object) {
        
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        JsonElement element = new JsonParser().parse(s);
        Object response;

        if(element.isJsonObject()) {
            response = getResponse(element.getAsJsonObject());
        } else if(element.isJsonArray()) {
            LinkedList<JsonRpcResponse> responses = new LinkedList<>();

            for(JsonElement call : element.getAsJsonArray()) {
                if(call.isJsonObject()) {
                    responses.add(getResponse(call.getAsJsonObject()));
                } else {
                    responses.add(new JsonRpcError(null, JSONRPC_INVALID_REQUEST, "Request is not an object."));
                }
            }

            response = new JsonRpcResponse[responses.size()];
            responses.toArray((JsonRpcResponse[]) response);
        } else {
            response = new JsonRpcError(null, JSONRPC_INVALID_REQUEST, "Root element is not an array or object.");
        }

        webSocket.send(new Gson().toJson(response));
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        handler.onError(webSocket, e);
    }
}
