
package transport;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hongha912
 */
public class PacketTest {
    
    public PacketTest() {
    }

    /**
     * Test of getAcknum method, of class Packet.
     */
    @Test
    public void testGetAcknum() {
        System.out.println("getAcknum");
        Packet instance = new Packet(new Message("test"), 0, 1);
        int expResult = 1;
        int result = instance.getAcknum();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSeqnum method, of class Packet.
     */
    @Test
    public void testGetSeqnum() {
        System.out.println("getSeqnum");
        Packet instance = new Packet(new Message("test"), 0, 1);
        int expResult = 0;
        int result = instance.getSeqnum();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMessage method, of class Packet.
     */
    @Test
    public void testGetMessage() {
        System.out.println("getMessage");
        Packet instance = new Packet(new Message("test"), 0, 1);
        Message expResult = new Message("test");
        Message result = instance.getMessage();
        assertEquals(expResult.getMessage(), result.getMessage());
    }

    /**
     * Test of corrupt method, of class Packet.
     */
    @Test
    public void testCorrupt() {
        System.out.println("corrupt");
        Packet instance =  new Packet(new Message("test"), 0, 1);
        instance.corrupt();
        assertTrue(instance.isCorrupt());
    }
    
    
    /**
     * Test of corrupt method, of class Packet.
     */
    @Test
    public void testCorrupt_2() {
        System.out.println("corrupt_notCorrupt");
        Packet instance =  new Packet(new Message("test"), 0, 1);
        assertTrue(!instance.isCorrupt());
    }
}
