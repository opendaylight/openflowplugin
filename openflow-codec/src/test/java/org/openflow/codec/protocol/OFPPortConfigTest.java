package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPPortMod;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.OFTestCase;

public class OFPPortConfigTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPPortMod msg = (OFPPortMod) messageFactory.getMessage(OFPType.PORT_MOD);
        msg.setHardwareAddress(new byte[6]);
        msg.portNumber = 1;
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.PORT_MOD, msg.getType());
        TestCase.assertEquals(1, msg.getPortNumber());
    }
}
