package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.junit.Test;
import org.openflow.codec.protocol.OFPHello;
import org.openflow.codec.protocol.OFPMessage;
import org.openflow.codec.protocol.OFPType;

public class OFPTypeTest extends TestCase {

    public void testOFTypeCreate() throws Exception {
        OFPType foo = OFPType.HELLO;
        Class<? extends OFPMessage> c = foo.toClass();
        TestCase.assertEquals(c, OFPHello.class);
    }

    @Test
    public void testMapping() throws Exception {
        TestCase.assertEquals(OFPType.HELLO, OFPType.valueOf((byte) 0));
        TestCase.assertEquals(OFPType.BARRIER_REPLY, OFPType.valueOf((byte) 21));
    }
}
