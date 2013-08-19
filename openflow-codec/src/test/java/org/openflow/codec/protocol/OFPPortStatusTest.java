package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPPort;
import org.openflow.codec.protocol.OFPPortStatus;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.OFPPortStatus.OFPortReason;
import org.openflow.codec.util.OFTestCase;

public class OFPPortStatusTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPPortStatus msg = (OFPPortStatus) messageFactory.getMessage(OFPType.PORT_STATUS);
        msg.setDesc(new OFPPort());
        msg.getDesc().setHardwareAddress(new byte[6]);
        msg.getDesc().setName("eth0");
        msg.setReason((byte) OFPortReason.OFPPR_ADD.ordinal());
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.PORT_STATUS, msg.getType());
        TestCase.assertEquals((byte) OFPortReason.OFPPR_ADD.ordinal(), msg.getReason());
        TestCase.assertNotNull(msg.getDesc());
    }
}
