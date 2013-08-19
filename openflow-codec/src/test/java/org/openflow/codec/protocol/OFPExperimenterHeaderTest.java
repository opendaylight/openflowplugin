package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPExperimenterHeader;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.util.OFTestCase;

public class OFPExperimenterHeaderTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPExperimenterHeader msg = (OFPExperimenterHeader) messageFactory.getMessage(OFPType.EXPERIMENTER);
        msg.setExperimenter(1);
        msg.setExpType(2);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(1, msg.getExperimenter());
        TestCase.assertEquals(2, msg.getExpType());
    }
}
