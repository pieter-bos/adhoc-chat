package client;

import client.protocol.*;
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

    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        state.clientLeft();
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(e.getMessage());
    }

    @Expose
    public String updateNickname(String nickname) {
        if (state.setNickname(nickname)) {
            return nickname;
        } else {
            return "";
        }
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

    @Expose
    public void addConversation(String user) {
        int id = state.addConversation(user);
        rpc.notify("newConversation", user, new String[0], id);
    }

    @Expose
    public void leaveConversation(int convId) {
        state.leaveConversation(convId);
    }

    public void newConversation(Conversation newConv) {
        rpc.notify("newConversation", newConv);
    }

    public void newUser(NickChangeMessage message) {
        rpc.notify("newUser", message.getUsername());
    }

    public void removeUser(LeaveMessage message) {
        rpc.notify("removeUser", message.getUsername());
    }

    public void newMessage(Conversation conv, String user, String message) {
        rpc.notify("newMessage", conv.getId(), user, message);
    }

    public void invite(InviteMessage inviteMessage) {
        rpc.notify("newConversation", inviteMessage.getOther(), new String[0], inviteMessage.getConversation());
    }

    public void leaveConversation(LeaveConversationMessage leaveConversationMessage) {
        rpc.notify("leaveConversation", leaveConversationMessage.getConvId());
    }
}
