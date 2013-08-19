package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFBMatchFields;
import org.openflow.codec.protocol.OFPFlowRemoved;
import org.openflow.codec.protocol.OFPMatch;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.OXMClass;
import org.openflow.codec.protocol.OXMField;
import org.openflow.codec.protocol.OFPFlowRemoved.OFFlowRemovedReason;
import org.openflow.codec.util.OFTestCase;

public class OFPFlowRemovedTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPFlowRemoved msg = (OFPFlowRemoved) messageFactory.getMessage(OFPType.FLOW_REMOVED);
        msg.setMatch(new OFPMatch());
        byte[] hwAddr = new byte[6];
        OXMField ethSrc = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.ETH_SRC, false, hwAddr);
        OXMField ethDst = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.ETH_DST, false, hwAddr);
        msg.getMatch().addMatchField(ethSrc);
        msg.getMatch().addMatchField(ethDst);
        msg.setReason(OFFlowRemovedReason.OFPRR_DELETE);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals(OFPType.FLOW_REMOVED, msg.getType());
        TestCase.assertEquals(OFFlowRemovedReason.OFPRR_DELETE, msg.getReason());
    }
}
