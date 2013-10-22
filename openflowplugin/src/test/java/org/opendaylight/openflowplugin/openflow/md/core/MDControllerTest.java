package org.opendaylight.openflowplugin.openflow.md.core;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDControllerTest {
    private static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImplTest.class);

    protected MDController controller;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        controller = new MDController();
        controller.init();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        controller = null;
    }


    /**
     * Test method for
     * {@link org.opendaylight.openflowplugin.openflow.md.core.MDController#addMessageListeners}
     * .
     */
    @Test
    public void testAddMessageListeners() {
            // Empty map
            int size = controller.getMessageListeners().size();
            Assert.assertEquals(size, 0);
            // Add one
            IMDMessageListener objDps = new DataPacketService() ;
            controller.addMessageListener(PacketIn.class, objDps);
            size = controller.getMessageListeners().size();
            Assert.assertEquals(size, 1);
            // Remove one
            controller.removeMessageListener(PacketIn.class, objDps);
            size = controller.getMessageListeners().size();
            Assert.assertEquals(size, 0);
            // Add two and remove One
            IMDMessageListener objFps = new FlowProgrammerService();
            controller.addMessageListener(PacketIn.class, objDps);
            controller.addMessageListener(FlowRemoved.class, objFps);
            controller.removeMessageListener(FlowRemoved.class, objFps);
            size = controller.getMessageListeners().size();
            Assert.assertEquals(size, 1);
            // Add one more and remove both
            controller.addMessageListener(FlowRemoved.class, objFps);
            controller.removeMessageListener(PacketIn.class, objDps);
            controller.removeMessageListener(FlowRemoved.class, objFps);
            size = controller.getMessageListeners().size();
            Assert.assertEquals(size, 0);
            // Add multiple listeners to messageTypes
            controller.addMessageListener(PacketIn.class, objDps);
            controller.addMessageListener(PacketIn.class, objFps); // Duplicate value entry
            controller.addMessageListener(FlowRemoved.class, objFps);
            size = controller.getMessageListeners().size();
            Assert.assertEquals(size, 2);
            // Remove one of the multiple listener, still size remains same
            controller.removeMessageListener(PacketIn.class, objFps);
            size = controller.getMessageListeners().size();
            Assert.assertEquals(size, 2);

    }

    private class DataPacketService implements IMDMessageListener {
        @Override
        public void receive(byte[] cookie, SessionContext sw, DataObject msg) {
            LOG.debug("Received a packet in DataPacket Service");
        }
    }

    private class FlowProgrammerService implements IMDMessageListener {
        @Override
        public void receive(byte[] cookie, SessionContext sw, DataObject msg) {
            LOG.debug("Received a packet in Flow Programmer Service");
        }
    }



}
