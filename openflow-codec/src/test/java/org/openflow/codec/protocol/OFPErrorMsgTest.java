package org.openflow.codec.protocol;

import java.util.List;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPErrorMsg;
import org.openflow.codec.protocol.OFPHello;
import org.openflow.codec.protocol.OFPMessage;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.OFPErrorMsg.OFErrorType;
import org.openflow.codec.protocol.OFPErrorMsg.OFHelloFailedCode;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.protocol.factory.OFPMessageFactory;
import org.openflow.codec.util.OFTestCase;

public class OFPErrorMsgTest extends OFTestCase {
    public void testWriteRead() throws Exception {
        OFPErrorMsg msg = (OFPErrorMsg) messageFactory.getMessage(OFPType.ERROR);
        msg.setMessageFactory(messageFactory);
        msg.setErrorType((short) OFErrorType.OFPET_HELLO_FAILED.ordinal());
        msg.setErrorCode((short) OFHelloFailedCode.OFPHFC_INCOMPATIBLE.ordinal());
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals((short) OFErrorType.OFPET_HELLO_FAILED.ordinal(), msg.getErrorType());
        TestCase.assertEquals((short) OFHelloFailedCode.OFPHFC_INCOMPATIBLE.ordinal(), msg.getErrorType());
        TestCase.assertNull(msg.getOffendingMsg(bb));

        msg.setOffendingMsg(new OFPHello(), bb);
        bb.clear();
        msg.writeTo(bb);
        bb.flip();
        msg.readFrom(bb);
        TestCase.assertEquals((short) OFErrorType.OFPET_HELLO_FAILED.ordinal(), msg.getErrorType());
        TestCase.assertEquals((short) OFHelloFailedCode.OFPHFC_INCOMPATIBLE.ordinal(), msg.getErrorType());
        TestCase.assertNotNull(msg.getOffendingMsg(bb));
        TestCase.assertEquals(OFPHello.MINIMUM_LENGTH, msg.getOffendingMsg(bb).length);
    }

    public void testGarbageAtEnd() {
        // This is a OFPErrorMsg msg (12 bytes), that encaps a OFVendor msg (24
        // bytes)
        // AND some zeros at the end (40 bytes) for a total of 76 bytes
        // THIS is what an NEC sends in reply to Nox's VENDOR request
        byte[] oferrorRaw = { 0x01, 0x01, 0x00, 0x4c, 0x00, 0x00, 0x10, (byte) 0xcc, 0x00, 0x01, 0x00, 0x01, 0x01,
                0x04, 0x00, 0x18, 0x00, 0x00, 0x10, (byte) 0xcc, 0x00, 0x00, 0x23, 0x20, 0x00, 0x00, 0x00, 0x08, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        OFPMessageFactory factory = new OFPBasicFactoryImpl();
        IDataBuffer buffer = DataBuffers.allocate(1024).wrap(oferrorRaw);

        List<OFPMessage> msgs = factory.parseMessages(buffer, oferrorRaw.length);
        TestCase.assertEquals(1, msgs.size());
        OFPMessage msg = msgs.get(0);
        TestCase.assertEquals(76, msg.getLengthU());
        IDataBuffer buffer1 = DataBuffers.allocate(1024);
        msg.writeTo(buffer1);
        TestCase.assertEquals(76, buffer1.position());
    }
}
