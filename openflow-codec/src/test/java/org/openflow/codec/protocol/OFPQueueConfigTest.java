package org.openflow.codec.protocol;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPQueueConfigReply;
import org.openflow.codec.protocol.OFPQueueConfigRequest;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.protocol.queue.OFPPacketQueue;
import org.openflow.codec.protocol.queue.OFPQueueProperty;
import org.openflow.codec.protocol.queue.OFPQueuePropertyMinRate;
import org.openflow.codec.protocol.queue.OFPQueuePropertyType;
import org.openflow.codec.util.OFTestCase;

public class OFPQueueConfigTest extends OFTestCase {
    public void testRequest() throws Exception {
        OFPQueueConfigRequest req = new OFPQueueConfigRequest();
        req.setPort((short) 5);
        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        req.writeTo(bb);
        bb.flip();

        OFPQueueConfigRequest req2 = new OFPQueueConfigRequest();
        req2.readFrom(bb);
        TestCase.assertEquals(req, req2);
    }

    public void testReply() throws Exception {
        OFPQueueConfigReply reply = new OFPQueueConfigReply();
        reply.setPort((short) 5);

        OFPPacketQueue queue = new OFPPacketQueue();
        queue.setQueueId(1);
        List<OFPQueueProperty> properties = new ArrayList<OFPQueueProperty>();
        properties.add(new OFPQueuePropertyMinRate().setRate((short) 1));
        queue.setProperties(properties);
        queue.setLength((short) (OFPPacketQueue.MINIMUM_LENGTH + OFPQueuePropertyMinRate.MINIMUM_LENGTH));

        List<OFPPacketQueue> queues = new ArrayList<OFPPacketQueue>();
        queues.add(queue);
        reply.setQueues(queues);
        reply.setLengthU(OFPQueueConfigReply.MINIMUM_LENGTH + queue.getLength());

        IDataBuffer bb = DataBuffers.allocate(1024);
        bb.clear();
        reply.writeTo(bb);
        bb.flip();

        OFPQueueConfigReply reply2 = new OFPQueueConfigReply();
        reply2.setQueuePropertyFactory(new OFPBasicFactoryImpl());
        reply2.readFrom(bb);
        TestCase.assertEquals(reply, reply2);
        TestCase.assertEquals(1, reply2.getQueues().size());
        TestCase.assertEquals(1, reply2.getQueues().get(0).getProperties().size());
        TestCase.assertTrue(reply2.getQueues().get(0).getProperties().get(0) instanceof OFPQueuePropertyMinRate);
        TestCase.assertEquals(OFPQueuePropertyType.MIN_RATE, reply2.getQueues().get(0).getProperties().get(0).getType());

        reply.getQueues().add(queue.clone());
        reply.setLengthU(reply.getLengthU() + queue.getLength());
        bb.clear();
        reply.writeTo(bb);
        bb.flip();
        reply2 = new OFPQueueConfigReply();
        reply2.setQueuePropertyFactory(new OFPBasicFactoryImpl());
        reply2.readFrom(bb);
        TestCase.assertEquals(reply, reply2);
        TestCase.assertEquals(2, reply2.getQueues().size());

        queue.getProperties().add(new OFPQueuePropertyMinRate().setRate((short) 2));
        queue.setLength((short) (queue.getLength() + OFPQueuePropertyMinRate.MINIMUM_LENGTH));
        reply.setLengthU(reply.getLengthU() + OFPQueuePropertyMinRate.MINIMUM_LENGTH);
        bb.clear();
        reply.writeTo(bb);
        bb.flip();
        reply2 = new OFPQueueConfigReply();
        reply2.setQueuePropertyFactory(new OFPBasicFactoryImpl());
        reply2.readFrom(bb);
        TestCase.assertEquals(reply, reply2);
        TestCase.assertEquals(2, reply2.getQueues().size());
        TestCase.assertEquals(2, reply2.getQueues().get(0).getProperties().size());
    }
}
