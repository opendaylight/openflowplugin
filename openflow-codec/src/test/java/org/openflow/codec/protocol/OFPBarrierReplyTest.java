package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPBarrierReply;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.OFTestCase;

public class OFPBarrierReplyTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPBarrierReply msg = (OFPBarrierReply) messageFactory.getMessage(OFPType.BARRIER_REPLY);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.BARRIER_REPLY, msg.getType());
    }
}
