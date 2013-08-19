package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPSetConfig;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.OFTestCase;

public class OFPGetConfigReplyTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPSetConfig msg = (OFPSetConfig) messageFactory.getMessage(OFPType.SET_CONFIG);
        msg.setFlags((short) 1);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.SET_CONFIG, msg.getType());
        TestCase.assertEquals((short) 1, msg.getFlags());
    }
}
