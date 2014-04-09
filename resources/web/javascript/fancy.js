var rpc = new WebSocketJSONRPC('ws://localhost:8081/');

rpc.on('error', function(err) {
    console.log('Error:', err);
});

rpc.on('open', function() {
    rpc.call('test', 1, 2, 3, function(result) {
        console.log(result);
    });
});