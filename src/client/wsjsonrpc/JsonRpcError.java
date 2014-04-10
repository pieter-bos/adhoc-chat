package client.wsjsonrpc;

/**
 * Created by pieter on 4/10/14.
 */
public class JsonRpcError extends JsonRpcResponse {
    private final Object error;

    public JsonRpcError(String id, int code, String message) {
        super(id);
        this.error = new JsonRpcErrorObject(code, message);
    }

    private class JsonRpcErrorObject {
        private final int code;
        private final String message;

        public JsonRpcErrorObject(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
