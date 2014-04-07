package test;

import org.junit.Test;
import transport.RawPacket;

public class RawPacketTest {
    @Test
    public void testGetData() throws Exception {
        RawPacket rp = new RawPacket((byte) 0, 0, 0, new byte[] {1, 2, 3, 4}, new byte[] {1, 2, 3, 4}, null);
        org.junit.Assert.assertNotNull(rp.getData());

        byte[] test = new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        rp = new RawPacket((byte) 0, 0, 0, new byte[] {1, 2, 3, 4}, new byte[] {1, 2, 3, 4}, test);
        org.junit.Assert.assertNotSame(test, rp.getData());
        org.junit.Assert.assertEquals(test, rp.getData());
    }
}
