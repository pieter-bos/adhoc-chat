package client.wsjsonrpc;

public class JsonRpcNotification {
    private final String jsonrpc = "2.0";
    private final String method;
    private final Object[] params;

    public JsonRpcNotification(String method, Object[] params) {
        this.method = method;
        this.params = params;
    }
}
