package org.openflow.codec.protocol;

import java.util.List;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPMessage;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.util.U16;

public class OFPBasicFactoryImplTest extends TestCase {
    public void testCreateAndParse() {
        OFPBasicFactoryImpl factory = new OFPBasicFactoryImpl();
        OFPMessage m = factory.getMessage(OFPType.HELLO);
        m.setVersion((byte) 1);
        m.setType(OFPType.ECHO_REQUEST);
        m.setLength(U16.t(8));
        m.setXid(0xdeadbeef);
        IDataBuffer bb = DataBuffers.allocate(1024);
        m.writeTo(bb);
        bb.flip();
        bb.limit(bb.limit() - 1);
        TestCase.assertEquals(0, factory.parseMessages(bb).size());
        bb.limit(bb.limit() + 1);
        List<OFPMessage> messages = factory.parseMessages(bb);
        TestCase.assertEquals(1, messages.size());
        TestCase.assertTrue(messages.get(0).getType() == OFPType.ECHO_REQUEST);
    }
}
