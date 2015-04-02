/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.connection.listener;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.openflowplugin.impl.connection.testutil.MsgGeneratorTestUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.connection.listener
 * <p/>
 * test of {@link OpenflowProtocolListenerImpl} - lightweight version, using basic ways (TDD)
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         <p/>
 *         Created: Mar 26, 2015
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenflowProtocolListenerImplTest {


    private OpenflowProtocolListenerImpl ofProtocolListener;

    @Mock
    private HandshakeContext handshakeContext;

    @Mock
    private ConnectionContext connectionContext;

    @Before
    public void initialize() {
        // place for mocking method's general behavior for HandshakeContext and ConnectionContext
        ofProtocolListener = new OpenflowProtocolListenerImpl(connectionContext, handshakeContext);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#OpenflowProtocolListenerImpl(org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext, org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext)}.
     */
    @Test
    @Ignore
    public void testOpenflowProtocolListenerImpl() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onEchoRequestMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoRequestMessage)}.
     */
    @Test
    @Ignore
    public void testOnEchoRequestMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onErrorMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage)}.
     */
    @Test
    @Ignore
    public void testOnErrorMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onExperimenterMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterMessage)}.
     */
    @Test
    @Ignore
    public void testOnExperimenterMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onFlowRemovedMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage)}.
     */
    @Test
    @Ignore
    public void testOnFlowRemovedMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onHelloMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage)}.
     */
    @Test
    @Ignore
    public void testOnHelloMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onMultipartReplyMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage)}.
     */
    @Test
    public void testOnMultipartReplyMessage() {
        final long xid = 1l;
        Mockito.when(connectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        final MultipartReply multipartReply = MsgGeneratorTestUtils.makeMultipartDescReply(xid, "test-val", false);
        ofProtocolListener.onMultipartReplyMessage((MultipartReplyMessage) multipartReply);
        Mockito.verify(connectionContext, Mockito.times(1)).addMultipartMsg(multipartReply);
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onPacketInMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketInMessage)}.
     */
    @Test
    @Ignore
    public void testOnPacketInMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#onPortStatusMessage(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortStatusMessage)}.
     */
    @Test
    @Ignore
    public void testOnPortStatusMessage() {
        fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.opendaylight.openflowplugin.impl.connection.listener.OpenflowProtocolListenerImpl#checkState(org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext.CONNECTION_STATE)}.
     */
    @Test
    @Ignore
    public void testCheckState() {
        fail("Not yet implemented");
    }

}
