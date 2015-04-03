/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device.listener;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceReplyProcessor;
import org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerInitialImpl;
import org.opendaylight.openflowplugin.impl.connection.testutil.MsgGeneratorTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.connection.listener
 * <p/>
 * test of {@link OpenflowProtocolListenerInitialImpl} - lightweight version, using basic ways (TDD)
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         <p/>
 *         Created: Mar 26, 2015
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenflowProtocolListenerFullImplTest {


    private OpenflowProtocolListenerFullImpl ofProtocolListener;

    @Mock
    private DeviceReplyProcessor deviceReplyProcessor;
    @Mock
    private ConnectionAdapter connectionAdapter;

    @Before
    public void setUp() {
        // place for mocking method's general behavior for HandshakeContext and ConnectionContext
        ofProtocolListener = new OpenflowProtocolListenerFullImpl(connectionAdapter, deviceReplyProcessor);
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#OpenflowProtocolListenerInitialImpl(org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext, org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext)}.
     */
    @Test
    @Ignore
    public void testOpenflowProtocolListenerImpl() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onEchoRequestMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage)}.
     */
    @Test
    @Ignore
    public void testOnEchoRequestMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onErrorMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage)}.
     */
    @Test
    @Ignore
    public void testOnErrorMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onExperimenterMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}.
     */
    @Test
    @Ignore
    public void testOnExperimenterMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onFlowRemovedMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage)}.
     */
    @Test
    @Ignore
    public void testOnFlowRemovedMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onHelloMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage)}.
     */
    @Test
    @Ignore
    public void testOnHelloMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onMultipartReplyMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage)}.
     */
    @Test
    public void testOnMultipartReplyMessage() {
        final long xid = 1l;
        ofProtocolListener.registerMultipartXid(xid);
        final MultipartReply multipartReply = MsgGeneratorTestUtils.makeMultipartDescReply(xid, "test-val", false);
        ofProtocolListener.onMultipartReplyMessage((MultipartReplyMessage) multipartReply);
        Mockito.verify(deviceReplyProcessor, Mockito.times(1)).processReply(Mockito.any(Xid.class), Mockito.anyListOf(MultipartReply.class));
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onPacketInMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage)}.
     */
    @Test
    @Ignore
    public void testOnPacketInMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#onPortStatusMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage)}.
     */
    @Test
    @Ignore
    public void testOnPortStatusMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link OpenflowProtocolListenerInitialImpl#checkState(org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext.CONNECTION_STATE)}.
     */
    @Test
    @Ignore
    public void testCheckState() {
        fail("Not yet implemented");
    }

}
