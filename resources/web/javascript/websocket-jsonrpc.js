function WebSocketJSONRPC(url) {
    this.socket = new WebSocket(url);
    this.callbacks = {};

    this._id = 0;

    this.newId = function() {
        return this._id++;
    }

    this.call = function(func) {
        var args = Array.prototype.slice.call(arguments, 1, arguments.length - 1);
        var callback = arguments[arguments.length - 1];
        var id = this.newId();

        this.callbacks[id] = callback;

        this.socket.send(JSON.stringify({
            jsonrpc: "2.0",
            method: func,
            params: args,
            id: id
        }));
    }
}