package client;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
                    OutputStream body = response.getOutputStream();
                    long time = System.currentTimeMillis();

                    String resource = !request.getPath().getPath().equals("/")
                            ? "/web" + request.getPath().getPath()
                            : "/web/index.html";

                    String mime = getMimeType(resource);
                    response.setValue("Content-Type", mime);
                    response.setDate("Date", time);
                    response.setDate("Last-Modified", time);
                    byte[] data = getFileContents(resource);
                    response.setValue("Content-Length", data.length + "");

                    body.write(data);
                    body.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private String getMimeType(String file) {
                String extension = file.substring(file.lastIndexOf(".") + 1);

                switch(extension) {
                    case "html":
                    case "htm":
                        return "text/html";
                    case "css":
                        return "text/css";
                    case "js":
                        return "text/javascript";
                    case "woff":
                        return "application/font-woff";
                    case "svg":
                        return "image/svg+xml";
                    case "png":
                        return "image/png";
                    case "ico":
                        return "image/vnd.microsoft.icon";
                    case "eot":
                        return "application/vnd.ms-fontobject";
                    case "ttf":
                        return "application/x-font-ttf";
                    case "gif":
                        return "image/gif";
                }

                return "application/octet-stream";
            }

            private byte[] getFileContents(String resource) throws IOException {
                InputStream input = getClass().getResourceAsStream(resource);
                ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

                byte[] data = new byte[1024];
                int read = 0;

                while((read = input.read(data)) > 0) {
                    bytesOut.write(data, 0, read);
                }

                return bytesOut.toByteArray();
            }
        };

        try {
            new SocketConnection(new ContainerServer(container)).connect(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
