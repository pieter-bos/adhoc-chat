package client.wsjsonrpc;

/**
 * Created by pieter on 4/10/14.
 */
public class JsonRpcError extends JsonRpcResponse {
    private final String jsonrpc = "2.0";
    private final Object error;
    private final String id;

    public JsonRpcError(String id, int code, String message) {
        this.error = new JsonRpcErrorObject(code, message);
        this.id = id;
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
