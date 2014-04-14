package client;

import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.SocketConnection;
import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

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

                    String resource = !request.getPath().getPath().equals("/")
                            ? "/web" + request.getPath().getPath()
                            : "/web/index.html";

                    if (resource.endsWith("html")) {
                        response.setValue("Content-Type", "text/html");
                    } else if (resource.endsWith("css")) {
                        response.setValue("Content-Type", "text/css");
                    }

                    response.setDate("Date", time);
                    response.setDate("Last-Modified", time);

                    body.append(getFileContents(resource));
                    body.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private String getFileContents(String resource) {
                Scanner sc = new Scanner(getClass().getResourceAsStream(resource));
                StringBuilder out = new StringBuilder();

                while (sc.hasNextLine()) {
                    out.append(sc.nextLine());
                }

                return out.toString();
            }
        };

        try {
            new SocketConnection(new ContainerServer(container)).connect(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
