function WebSocketJSONRPC(url) {
    this.open = false;
    this.socket = new WebSocket(url);

    this.callbacks = {};
    this.events = { error: [], close: [], open: [] };

    this._id = 0;

    this.socket.onmessage = (function(e) {
        var data = JSON.parse(e.data);

        if(data.error !== undefined) {
            this.emit('error', data.error);
        } else {
            this.callbacks[data.id](data.result);
        }
    }).bind(this);

    this.socket.onerror = (function(e) {
        this.emit('error', 'An error occurred on the WebSocket layer');
    }).bind(this);

    this.socket.onopen = (function(e) {
        this.open = true;
        this.emit('open');
    }).bind(this);

    this.socket.onclose = (function(e) {
        this.open = false;
        this.emit('close');
    }).bind(this);

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

    this.def = function(func) {
        this[func] = (function() {
            var args = Array.prototype.slice.call(arguments, 0, arguments.length);
            args.unshift(func);
            this.call.apply(this, args);
        }).bind(this);
    }
}