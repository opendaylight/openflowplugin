/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device.initialization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Future;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.datastore.multipart.AbstractMultipartWriter;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCaseBuilder;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class OF13DeviceInitializerTest {
    @Mock
    private DeviceContext deviceContext;
    @Mock
    private MultipartWriterProvider multipartWriterProvider;
    @Mock
    private AbstractMultipartWriter abstractMultipartWriter;
    @Mock
    private ConvertorExecutor convertorExecutor;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private FeaturesReply featuresReply;
    @Mock
    private Capabilities capabilities;
    @Mock
    private DeviceInfo deviceInfo;
    @Mock
    private DeviceState deviceState;
    @Mock
    private MessageSpy messageSpy;
    @Mock
    private TranslatorLibrary translatorLibrary;
    @Mock
    private MessageTranslator messageTranslator;
    @Mock
    private RequestContext requestContext;
    @Mock
    private OutboundQueueProvider outboundQueueProvider;
    private AbstractDeviceInitializer deviceInitializer;

    @Before
    public void setUp() {
        final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier = DeviceStateUtil
                .createNodeInstanceIdentifier(new NodeId("openflow:1"));

        deviceInitializer = new OF13DeviceInitializer();
        when(multipartWriterProvider.lookup(any())).thenReturn(Optional
                .of(abstractMultipartWriter));
        when(featuresReply.getCapabilities()).thenReturn(capabilities);
        when(connectionContext.getFeatures()).thenReturn(featuresReply);
        when(connectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        when(deviceContext.getDeviceState()).thenReturn(deviceState);
        when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeInstanceIdentifier);
        when(deviceInfo.getNodeId()).thenReturn(nodeInstanceIdentifier.getKey().getId());
        when(deviceContext.getDeviceInfo()).thenReturn(deviceInfo);
        when(deviceContext.getMessageSpy()).thenReturn(messageSpy);
        when(deviceContext.oook()).thenReturn(translatorLibrary);
        when(requestContext.getXid()).thenReturn(new Xid(Uint32.valueOf(42L)));
        when(deviceContext.createRequestContext()).thenReturn(requestContext);
        when(deviceContext.getPrimaryConnectionContext()).thenReturn(connectionContext);
    }

    @Test
    public void initializeSingleLayer() throws Exception {
        when(deviceContext.canUseSingleLayerSerialization()).thenReturn(true);
        when(requestContext.getFuture()).thenReturn(RpcResultBuilder
                .success(Collections.singletonList(new MultipartReplyBuilder()
                        .setMultipartReplyBody(new MultipartReplyDescBuilder().build())
                        .build()))
                .buildFuture());

        final Future<Void> initialize = deviceInitializer.initialize(deviceContext,
                true,
                false,
                multipartWriterProvider,
                convertorExecutor);

        initialize.get();
        verify(messageSpy, times(5)).spyMessage(eq(MultipartType.class), any());
        verify(requestContext, times(5)).getFuture();
        verify(featuresReply).getCapabilities();
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }

    @Test
    public void initializeMultiLayer() throws Exception {
        when(deviceContext.canUseSingleLayerSerialization()).thenReturn(false);
        when(requestContext.getFuture()).thenReturn(RpcResultBuilder
                .success(Collections.singletonList(new MultipartReplyMessageBuilder()
                        .setType(MultipartType.OFPMPDESC)
                        .setMultipartReplyBody(new MultipartReplyDescCaseBuilder()
                                .setMultipartReplyDesc(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow
                                        .protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply
                                        .desc._case.MultipartReplyDescBuilder()
                                        .build())
                                .build())
                        .build()))

                .buildFuture());
        final Future<Void> initialize = deviceInitializer.initialize(deviceContext,
                true,
                false,
                multipartWriterProvider,
                convertorExecutor);

        initialize.get();
        verify(messageSpy, times(5)).spyMessage(eq(MultipartType.class), any());
        verify(requestContext, times(5)).getFuture();
        verify(featuresReply).getCapabilities();
        verify(deviceContext).writeToTransaction(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}