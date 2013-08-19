package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPGetConfigRequest;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.OFTestCase;

public class OFPGetConfigRequestTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPGetConfigRequest msg = (OFPGetConfigRequest) messageFactory.getMessage(OFPType.GET_CONFIG_REQUEST);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.GET_CONFIG_REQUEST, msg.getType());
    }
}
