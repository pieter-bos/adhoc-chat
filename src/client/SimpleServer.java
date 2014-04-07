package client;

import com.sun.net.httpserver.HttpServer;
import sun.net.httpserver.HttpServerImpl;

/**
 *
 */
public class SimpleServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServerImpl();
    }
}
