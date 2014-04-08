package client;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;

/**
 * Serves static files for the user interface
 */
public class SimpleServer {
    /**
     * Creates a new http server that serves files on port
     * @param port
     */
    public SimpleServer(int port) {
        Container container = new Container() {
            @Override
            public void handle(Request request, Response response) {
                try {
                    PrintStream body = response.getPrintStream();
                    long time = System.currentTimeMillis();

                    response.setValue("Content-Type", "text/plain");
                    response.setValue("Server", "HelloWorld/1.0 (Simple 4.0)");
                    response.setDate("Date", time);
                    response.setDate("Last-Modified", time);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            new SocketConnection(new ContainerServer(container)).connect(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
