package transport_v2;

import java.io.IOException;
import java.net.InetAddress;

public class RTTTest {
    public static final boolean isFrans = false;
    public static final String FRANS = "192.168.5.13";
    public static final String PIETER = "192.168.5.123";
    private static final int DATA_LENGTH = 64;

    private final SocketImpl socket;
    private InetAddress frans;
    private InetAddress pieter;

    public RTTTest() throws InterruptedException, IOException {
        socket = new SocketImpl(1234);
        socket.connect();
        frans = InetAddress.getByName(FRANS);
        pieter = InetAddress.getByName(PIETER);

        Thread.sleep(3000);

        long lastTick = System.nanoTime();

        while(true) {
            if(isFrans) {
                send();
                recv();
            } else {
                recv();
                send();
            }

            long newTick = System.nanoTime();
            System.out.println((double) (newTick - lastTick) / 1000000.0);
            lastTick = newTick;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new RTTTest();
    }

    private void recv() {
        socket.receive();
    }

    private void send() throws IOException {
        socket.send(new byte[DATA_LENGTH], isFrans ? pieter : frans);
    }
}
