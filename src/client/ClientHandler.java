package client;

import client.protocol.TextMessage;
import client.wsjsonrpc.Expose;
import client.wsjsonrpc.WebSocketJsonRpc;
import client.wsjsonrpc.WebSocketJsonRpcHandler;
import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

/**
 * ClientHandler handles traffic between the browser and between the client.
 */
public class ClientHandler implements WebSocketJsonRpcHandler {
    private ApplicationState state;
    private WebSocketJsonRpc<ClientHandler> rpc;

    public ClientHandler(ApplicationState state) {
        this.state = state;
    }

    public void setSocket(WebSocketJsonRpc<ClientHandler> rpc) {
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
        System.out.println(e.getMessage());
    }

    @Expose
    public boolean updateNickname(String nickname) {
        state.setNickname(nickname);

        return true;
    }

    @Expose
    public String sendMessage(String data, int convId) {
        TextMessage message = new TextMessage(data, state.getNickname(), convId);
        state.sendMessage(message);
        return new Gson().toJson(message);
    }

    @Expose
    public String getConversations() {
        return new Gson().toJson(state.getConversationList().values());
    }

    @Expose
    public String getUsers() {
        return new Gson().toJson(state.getUsers().values());
    }

    public void newConversation(Conversation newConv) {
        rpc.notify("newConversation", newConv);
    }
}
