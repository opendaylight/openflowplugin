package org.openflow.codec.protocol;

import org.openflow.codec.util.OFTestCase;

public class OFPSwitchFeaturesReplyTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        // OFPSwitchFeaturesReply ofr = (OFPSwitchFeaturesReply) messageFactory
        // .getMessage(OFPType.FEATURES_REPLY);
        // List<OFPPort> ports = new ArrayList<OFPPort>();
        // OFPPort port = new OFPPort();
        // port.setHardwareAddress(new byte[6]);
        // port.setName("eth0");
        // ports.add(port);
        // ofr.setPorts(ports);
        // IDataBuffer bb = DataBuffers.createBuffer(1024);
        // bb.clear();
        // ofr.writeTo(bb);
        // bb.flip();
        // ofr.readFrom(bb);
        // TestCase.assertEquals(1, ofr.getPorts().size());
        // TestCase.assertEquals("eth0", ofr.getPorts().get(0).getName());
        //
        // // test a 15 character name
        // ofr.getPorts().get(0).setName("012345678901234");
        // bb.clear();
        // ofr.writeTo(bb);
        // bb.flip();
        // ofr.readFrom(bb);
        // TestCase.assertEquals("012345678901234",
        // ofr.getPorts().get(0).getName());
        //
        // // test a 16 character name getting truncated
        // ofr.getPorts().get(0).setName("0123456789012345");
        // bb.clear();
        // ofr.writeTo(bb);
        // bb.flip();
        // ofr.readFrom(bb);
        // TestCase.assertEquals("012345678901234",
        // ofr.getPorts().get(0).getName());
    }
}
