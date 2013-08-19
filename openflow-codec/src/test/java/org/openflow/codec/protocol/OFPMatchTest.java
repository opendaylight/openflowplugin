package org.openflow.codec.protocol;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFBMatchFields;
import org.openflow.codec.protocol.OFPMatch;
import org.openflow.codec.protocol.OXMClass;
import org.openflow.codec.protocol.OXMField;

public class OFPMatchTest extends TestCase {

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        buffer.clear();
    }

    public void testClone() {
        OFPMatch match1 = new OFPMatch();
        OFPMatch match2 = match1.clone();
        TestCase.assertEquals(match1, match2);
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        match2.addMatchField(matchField);
        TestCase.assertNotSame(match1, match2);
        match1 = match2.clone();
        TestCase.assertEquals(match1, match2);
    }

    public void testReadWrite() {
        OFPMatch match1 = new OFPMatch();
        OFPMatch match2 = new OFPMatch();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        match1.addMatchField(matchField);
        match1.writeTo(buffer);
        buffer.flip();
        match2.readFrom(buffer);
        TestCase.assertEquals(match1, match2);
    }

    public void testToString() {
        OFPMatch match1 = new OFPMatch();
        OFPMatch match2 = new OFPMatch();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        match1.addMatchField(matchField);
        match1.writeTo(buffer);
        buffer.flip();
        match2.readFrom(buffer);
        TestCase.assertEquals(match1.toString(), match2.toString());
    }

    public void testEqualHashcode() {
        OFPMatch match1 = new OFPMatch();
        OFPMatch match2 = new OFPMatch();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        match1.addMatchField(matchField);
        OXMField matchField1 = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_DST, false, new byte[] { 1, 2,
                3, 4 });
        match1.addMatchField(matchField1);
        match1.writeTo(buffer);
        buffer.flip();
        match2.readFrom(buffer);
        TestCase.assertTrue(match1.equals(match2));
        TestCase.assertEquals(match1.hashCode(), match2.hashCode());
    }

    public void testPrerequisite() {
        OFPMatch match1 = new OFPMatch();
        OFPMatch match2 = new OFPMatch();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        match1.addMatchField(matchField);
        OXMField matchField1 = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_DST, false, new byte[] { 1, 2,
                3, 4 });
        match1.addMatchField(matchField1);
        match1.writeTo(buffer);
        buffer.flip();
        boolean result = match2.readFrom(buffer);
        TestCase.assertTrue(result);
    }

    public void testFromString() {
        // OFPMatch correct = new OFPMatch();
        // OFPMatch tester = new OFPMatch();
        //
        // // Various combinations of "all"/"any"
        // tester.fromString("OFPMatch[]");
        // // correct is already wildcarded
        // TestCase.assertEquals(correct, tester);
        // tester.fromString("all");
        // TestCase.assertEquals(correct, tester);
        // tester.fromString("ANY");
        // TestCase.assertEquals(correct, tester);
        // tester.fromString("");
        // TestCase.assertEquals(correct, tester);
        // tester.fromString("[]");
        // TestCase.assertEquals(correct, tester);
        //
        // // ip_src
        // correct.setWildcards(~OFPMatch.OFPFW_NW_SRC_MASK);
        // correct.setNetworkSource(0x01010203);
        // tester.fromString("nw_src=1.1.2.3");
        // TestCase.assertEquals(correct.getNetworkSourceMaskLen(), tester
        // .getNetworkSourceMaskLen());
        // TestCase.assertEquals(correct, tester);
        // tester.fromString("IP_sRc=1.1.2.3");
        // TestCase.assertEquals(correct.getNetworkSourceMaskLen(), tester
        // .getNetworkSourceMaskLen());
        // TestCase.assertEquals(correct, tester);
    }

    public void testIpToString() {
        // String test = OFPMatch.ipToString(-1);
        // TestCase.assertEquals("255.255.255.255", test);
    }

    public void testReverse() {
        // OFPMatch match1 = new OFPMatch();
        // OFPMatch match2 = match1.reverse((short)0, true);
        // TestCase.assertEquals(match1, match2);
        //
        // match1.fromString("dl_dst=00:11:22:33:44:55");
        // match2 = match1.reverse((short)0, true);
        // OFPMatch match3 = new OFPMatch();
        // match3.fromString("dl_src=00:11:22:33:44:55");
        // TestCase.assertEquals(match2, match3);
        //
        // match1.fromString("nw_dst=192.168.0.0/24");
        // match2 = match1.reverse((short)0, true);
        // match3.fromString("nw_src=192.168.0.0/24");
        // TestCase.assertEquals(match2, match3);
        //
        // match1.fromString("in_port=1");
        // match2 = match1.reverse((short)2, false);
        // match3.fromString("in_port=2");
        // TestCase.assertEquals(match2, match3);
    }

    public void testSubsumes() {
        // OFPMatch match1 = new OFPMatch();
        // OFPMatch match2 = new OFPMatch();
        // match2.fromString("dl_dst=00:11:22:33:44:55");
        // TestCase.assertTrue(match1.subsumes(match2));
        // TestCase.assertFalse(match2.subsumes(match1));
        //
        // match1.fromString("nw_dst=192.168.0.0/16");
        // match2.fromString("nw_dst=192.168.0.0/24");
        // TestCase.assertTrue(match1.subsumes(match2));
        // TestCase.assertFalse(match2.subsumes(match1));
    }
}
