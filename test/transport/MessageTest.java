/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transport;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hongha912
 */
public class MessageTest {
    
    public MessageTest() {
    }

    /**
     * Test of getMessage method, of class Message.
     */
    @Test
    public void testGetMessage() {
        System.out.println("getMessage");
        Message instance = null;
        String expResult = "";
        String result = instance.getMessage();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of corruptMessage method, of class Message.
     */
    @Test
    public void testCorruptMessage() {
        System.out.println("corruptMessage");
        Message instance = null;
        instance.corruptMessage();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
