package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPBarrierRequest;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.OFTestCase;

public class OFPBarrierRequestTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPBarrierRequest msg = (OFPBarrierRequest) messageFactory.getMessage(OFPType.BARRIER_REQUEST);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.BARRIER_REQUEST, msg.getType());
    }
}
