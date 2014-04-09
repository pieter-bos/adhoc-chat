function WebSocketJSONRPC(url) {
    this.open = false;
    this.socket = new WebSocket(url);

    this.callbacks = {};
    this.events = { error: [], close: [], open: [] };

    this._id = 0;

    this.socket.onmessage = function(e) {
        var data = JSON.parse(e.data);

        console.log(data);
    }

    this.socket.onerror = function(e) {
        this.emit('error', e.message);
    }

    this.socket.onopen = function(e) {
        this.open = true;
        this.emit('open');
    }

    this.socket.onclose = function(e) {
        this.open = false;
        this.emit('close');
    }

    this.on = function(event, callback) {
        if(event in this.events) {
            this.events[event].push(callback);
        }
    }

    this.emit = function(event) {
        var args = Array.prototype.slice.call(arguments, 1, arguments.length);

        for(var callback in this.events[event]) {
            this.events[event][callback].apply(null, args);
        }
    }

    this.newId = function() {
        return this._id++;
    }

    this.call = function(func) {
        if(!this.open) {
            this.emit('error', 'Cannot call a function when the connection is not yet open.');
            return false;
        }

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

        return true;
    }
}