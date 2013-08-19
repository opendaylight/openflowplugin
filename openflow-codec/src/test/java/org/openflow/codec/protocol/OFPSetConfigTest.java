package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPGetConfigReply;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.OFTestCase;

public class OFPSetConfigTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPGetConfigReply msg = (OFPGetConfigReply) messageFactory.getMessage(OFPType.GET_CONFIG_REPLY);
        msg.setFlags((short) 1);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.GET_CONFIG_REPLY, msg.getType());
        TestCase.assertEquals((short) 1, msg.getFlags());
    }
}
