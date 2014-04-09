package transport;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import transport.RawPacket;

import java.util.Arrays;

@RunWith(org.junit.runners.JUnit4.class)
public class RawPacketTest {
    @Test
    public void testGetData() throws Exception {
        RawPacket rp = new RawPacket((byte) 0, 0, 0, 0, new byte[] {1, 2, 3, 4}, new byte[] {1, 2, 3, 4}, null);
        assertNotNull(rp.getData());
        assertTrue(Arrays.equals(new byte[]{}, rp.getData()));

        byte[] test = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        rp = new RawPacket((byte) 0, 0, 0, 0, new byte[] {1, 2, 3, 4}, new byte[] {1, 2, 3, 4}, test);
        assertNotSame(test, rp.getData());
        assertTrue(Arrays.equals(test, rp.getData()));
    }
}
