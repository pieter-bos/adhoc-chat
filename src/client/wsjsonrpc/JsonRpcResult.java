package client.wsjsonrpc;

public class JsonRpcResult extends JsonRpcResponse {
    private final Object result;

    public JsonRpcResult(String id, Object result) {
        super(id);
        this.result = result;
    }
}
