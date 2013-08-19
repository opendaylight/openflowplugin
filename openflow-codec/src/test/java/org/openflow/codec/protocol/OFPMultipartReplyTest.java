package org.openflow.codec.protocol;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.protocol.factory.OFPMessageFactory;
import org.openflow.codec.util.OFTestCase;

public class OFPMultipartReplyTest extends OFTestCase {
    public void testOFFlowStatisticsReply() throws Exception {
        byte[] packet = new byte[] { 0x01, 0x11, 0x01, 0x2c, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00,
                0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x02, (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x08, 0x00, 0x00, 0x01, 0x00, 0x00, 0x0a,
                0x00, 0x00, 0x03, 0x0a, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3a,
                (byte) 0xa6, (byte) 0xa6, 0x00, (byte) 0xff, (byte) 0xff, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xc4, 0x00, 0x00, 0x00, 0x08, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x08, 0x06, 0x00, 0x02,
                0x00, 0x00, 0x0a, 0x00, 0x00, 0x03, 0x0a, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x3b, 0x2f, (byte) 0xfa, 0x40, (byte) 0xff, (byte) 0xff, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3c, 0x00, 0x00, 0x00, 0x08, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, (byte) 0xff, (byte) 0xff, 0x00, 0x62, 0x08, 0x00, 0x00, 0x01,
                0x62, 0x37, 0x0a, 0x00, 0x00, 0x02, 0x0a, 0x00, 0x00, 0x03, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x3a, (byte) 0xc5, 0x2a, (byte) 0x80, (byte) 0xff, (byte) 0xff, 0x00, 0x05, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xc4, 0x00, 0x00, 0x00, 0x08,
                0x00, 0x02, 0x00, 0x00 };

        OFPMessageFactory factory = new OFPBasicFactoryImpl();
        IDataBuffer bb = DataBuffers.allocate(8);
        IDataBuffer packetBuf = bb.wrap(packet);
        /*
         * List<OFPMessage> msgs = factory.parseMessages(packetBuf,
         * packet.length); TestCase.assertEquals(1, msgs.size());
         * TestCase.assertTrue(msgs.get(0) instanceof OFStatisticsReply);
         * OFStatisticsReply sr = (OFStatisticsReply) msgs.get(0);
         * TestCase.assertEquals(OFStatisticsType.FLOW, sr.getStatisticType());
         * TestCase.assertEquals(3, sr.getStatistics().size());
         */
    }
}
