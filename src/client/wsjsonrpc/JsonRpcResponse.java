package client.wsjsonrpc;

public abstract class JsonRpcResponse {
    private final String jsonrpc = "2.0";
    private final String id;

    public JsonRpcResponse(String id) {
        this.id = id;
    }
}
