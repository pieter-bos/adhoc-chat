package client.wsjsonrpc;

import client.TestHandler;

import java.net.InetSocketAddress;

/**
 * Created by pieter on 4/10/14.
 */
public class Test {
    public static void main(String[] args) {
        WebSocketJsonRpc<TestHandler> rpc = new WebSocketJsonRpc<>(new InetSocketAddress(8081), new TestHandler(), TestHandler.class);
        rpc.start();
    }
}
