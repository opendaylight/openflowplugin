/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
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
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by mirehak on 7/23/15.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractStatsServiceTest {
    @Mock
    RequestContextStack rqContextStack;
    @Mock
    DeviceContext deviceContext;
    @Mock
    ConnectionContext connectionContext;
    @Mock
    FeaturesReply features;
    @Mock
    MessageSpy messageSpy;
    @Mock
    OutboundQueue outboundQueueProvider;
    @Mock
    MultiMsgCollector multiMsgCollector;
    @Mock
    TranslatorLibrary translatorLibrary;

    final Answer<Void> answerVoidToCallback = new Answer<Void>() {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            final FutureCallback<OfHeader> callback = (FutureCallback<OfHeader>) (invocation.getArguments()[2]);
            callback.onSuccess(null);
            return null;
        }
    };

    /**
     * default ctor
     */
    public AbstractStatsServiceTest() {
        OpenflowPortsUtil.init();
    }

    @Before
    public void init() throws Exception {
        Mockito.when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
        Mockito.when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        Mockito.when(deviceContext.getMultiMsgCollector(Matchers.any(RequestContext.class))).thenReturn(multiMsgCollector);
        Mockito.when(deviceContext.oook()).thenReturn(translatorLibrary);
        Mockito.when(connectionContext.getFeatures()).thenReturn(features);
        Mockito.when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        Mockito.when(features.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);

        setUp();
    }

    protected void setUp() {
        //NOOP
    }

    static NodeRef createNodeRef(String nodeIdValue) {
        InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(nodeIdValue)));
        return new NodeRef(nodePath);
    }
}
