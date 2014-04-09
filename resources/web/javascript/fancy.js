var rpc = new WebSocketJSONRPC('ws://localhost:8001/');
rpc.call('test', 1, 2, 3, function(result) {

});