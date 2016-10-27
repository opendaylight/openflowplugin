/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
import java.math.BigInteger;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by mirehak on 7/23/15.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractStatsServiceTest {
    @Mock
    protected RequestContextStack rqContextStack;
    @Mock
    protected DeviceContext deviceContext;
    @Mock
    protected ConnectionContext connectionContext;
    @Mock
    protected FeaturesReply features;
    @Mock
    private GetFeaturesOutput getFeaturesOutput;
    @Mock
    protected MessageSpy messageSpy;
    @Mock
    protected OutboundQueue outboundQueueProvider;
    @Mock
    protected MultiMsgCollector multiMsgCollector;
    @Mock
    protected TranslatorLibrary translatorLibrary;
    @Mock
    protected DeviceState deviceState;
    @Mock
    protected DeviceInfo deviceInfo;


    public static final NodeId NODE_ID = new NodeId("unit-test-node:123");

    protected final Answer<Void> answerVoidToCallback = new Answer<Void>() {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            final FutureCallback<OfHeader> callback = (FutureCallback<OfHeader>) (invocation.getArguments()[2]);
            callback.onSuccess(null);
            return null;
        }
    };

    @Before
    public void init() throws Exception {
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        Mockito.when(deviceContext.getMultiMsgCollector(Matchers.any(RequestContext.class))).thenReturn(multiMsgCollector);
        Mockito.when(deviceContext.oook()).thenReturn(translatorLibrary);
        Mockito.when(deviceContext.getDeviceState()).thenReturn(deviceState);
        Mockito.when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(NODE_ID);
        Mockito.when(deviceInfo.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(deviceInfo.getDatapathId()).thenReturn(BigInteger.TEN);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        Mockito.when(features.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        Mockito.when(getFeaturesOutput.getDatapathId()).thenReturn(BigInteger.valueOf(123L));
        Mockito.when(getFeaturesOutput.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);

        setUp();
    }

    protected void setUp() {
        //NOOP
    }

    protected static NodeRef createNodeRef(String nodeIdValue) {
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeIdValue)));
        return new NodeRef(nodePath);
    }
}
