/**
 * Copyright (c) 2013 IBM Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MDControllerTest {
    protected static final Logger LOG = LoggerFactory
            .getLogger(ConnectionConductorImplTest.class);

    protected MDController controller;


    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        controller = new MDController(convertorManager);
        controller.init();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        controller = null;
    }

    @Test
    public void testAddMessageListeners() {
        //clean translators
        controller.getMessageTranslators().clear();

        // Empty map
        int size = controller.getMessageTranslators().size();
        Assert.assertEquals(0, size);
        // Add one
        IMDMessageTranslator<OfHeader, List<DataObject>> objDps = new DataPacketService() ;
        controller.addMessageTranslator(PacketIn.class, 4, objDps);
        size = controller.getMessageTranslators().size();
        Assert.assertEquals(1, size);
        // Remove one
        controller.removeMessageTranslator(PacketIn.class, 4, objDps);
        size = controller.getMessageTranslators().size();
        Assert.assertEquals(0, size);
        // Add two and remove One
        IMDMessageTranslator objFps = new FlowProgrammerService();
        controller.addMessageTranslator(PacketIn.class, 4, objDps);
        controller.addMessageTranslator(FlowRemoved.class, 4, objFps);
        controller.removeMessageTranslator(FlowRemoved.class, 4, objFps);
        size = controller.getMessageTranslators().size();
        Assert.assertEquals(1, size);
        // Add one more and remove both
        controller.addMessageTranslator(FlowRemoved.class, 4, objFps);
        controller.removeMessageTranslator(PacketIn.class, 4, objDps);
        controller.removeMessageTranslator(FlowRemoved.class, 4, objFps);
        size = controller.getMessageTranslators().size();
        Assert.assertEquals(0, size);
        // Add multiple listeners to messageTypes
        controller.addMessageTranslator(PacketIn.class, 4, objDps);
        controller.addMessageTranslator(PacketIn.class, 4, objFps); // Duplicate value entry
        controller.addMessageTranslator(FlowRemoved.class, 4, objFps);
        size = controller.getMessageTranslators().size();
        Assert.assertEquals(2, size);
        // Remove one of the multiple listener, still size remains same
        controller.removeMessageTranslator(PacketIn.class, 4, objFps);
        size = controller.getMessageTranslators().size();
        Assert.assertEquals(2, size);
    }

    private class DataPacketService implements IMDMessageTranslator<OfHeader, List<DataObject>> {
        @Override
        public List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sw, OfHeader msg) {
            LOG.debug("Received a packet in DataPacket Service");
            return null;
        }
    }

    private class FlowProgrammerService implements IMDMessageTranslator<OfHeader, DataObject> {
        @Override
        public DataObject translate(SwitchConnectionDistinguisher cookie, SessionContext sw, OfHeader msg) {
            LOG.debug("Received a packet in Flow Programmer Service");
            return null;
        }
    }



}
