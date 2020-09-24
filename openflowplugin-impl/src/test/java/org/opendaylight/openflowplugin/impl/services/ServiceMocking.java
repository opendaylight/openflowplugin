/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorData;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.device.DeviceContextImpl;
import org.opendaylight.openflowplugin.impl.registry.flow.DeviceFlowRegistryImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

@RunWith(MockitoJUnitRunner.class)
public abstract class ServiceMocking {
    protected static final Uint64 DUMMY_DATAPATH_ID = Uint64.valueOf(444);
    protected static final Uint8 DUMMY_VERSION = Uint8.valueOf(OFConstants.OFP_VERSION_1_3);
    protected static final Uint32 DUMMY_XID_VALUE = Uint32.valueOf(2121L);
    protected static final Xid DUMMY_XID = new Xid(DUMMY_XID_VALUE);
    protected static final Uint32 DUMMY_EXPERIMENTER_ID = Uint32.valueOf(42);

    protected static final String DUMMY_NODE_ID = "444";
    protected static final KeyedInstanceIdentifier<Node, NodeKey> DUMMY_NODE_II = InstanceIdentifier
            .create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId(DUMMY_NODE_ID)));

    @Mock
    protected RequestContextStack mockedRequestContextStack;
    @Mock
    protected ConnectionContext mockedPrimConnectionContext;
    @Mock
    protected FeaturesReply mockedFeatures;
    @Mock
    protected GetFeaturesOutput mockedFeaturesOutput;
    @Mock
    protected ConnectionAdapter mockedConnectionAdapter;
    @Mock
    protected MessageSpy mockedMessagSpy;
    @Mock
    protected DeviceContextImpl mockedDeviceContext;
    @Mock
    protected DeviceState mockedDeviceState;
    @Mock
    protected DeviceInfo mockedDeviceInfo;
    @Mock
    protected RequestContext mockedRequestContext;
    @Mock
    protected OutboundQueue mockedOutboundQueue;
    @Mock
    protected MultiMsgCollector multiMessageCollector;
    @Mock
    protected DataBroker dataBroker;
    @Mock
    protected ExtensionConverterProvider mockedExtensionConverterProvider;
    @Mock
    protected ConverterMessageToOFJava<ExperimenterMessageOfChoice, DataContainer,
        ConvertorData> mockedExtensionConverter;

    @Before
    @SuppressWarnings("unchecked")
    public void initialization() {
        lenient().when(mockedExtensionConverter.getExperimenterId())
                .thenReturn(new ExperimenterId(DUMMY_EXPERIMENTER_ID));
        lenient().when(mockedExtensionConverterProvider.getMessageConverter(Matchers.<TypeVersionKey>any()))
                .thenReturn(mockedExtensionConverter);
        lenient().when(mockedRequestContextStack.createRequestContext()).thenReturn(mockedRequestContext);
        lenient().when(mockedRequestContext.getXid()).thenReturn(DUMMY_XID);
        lenient().when(mockedFeatures.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        lenient().when(mockedFeatures.getVersion()).thenReturn(DUMMY_VERSION);

        lenient().when(mockedFeaturesOutput.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        lenient().when(mockedFeaturesOutput.getVersion()).thenReturn(DUMMY_VERSION);

        lenient().when(mockedPrimConnectionContext.getFeatures()).thenReturn(mockedFeatures);
        lenient().when(mockedPrimConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        lenient().when(mockedPrimConnectionContext.getConnectionState())
                .thenReturn(ConnectionContext.CONNECTION_STATE.WORKING);
        lenient().when(mockedPrimConnectionContext.getOutboundQueueProvider()).thenReturn(mockedOutboundQueue);

        lenient().when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(DUMMY_NODE_II);
        when(mockedDeviceInfo.getDatapathId()).thenReturn(DUMMY_DATAPATH_ID);
        when(mockedDeviceInfo.getVersion()).thenReturn(DUMMY_VERSION.toJava());

        lenient().when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockedPrimConnectionContext);
        when(mockedDeviceContext.getMessageSpy()).thenReturn(mockedMessagSpy);
        lenient().when(mockedDeviceContext.getDeviceFlowRegistry())
                .thenReturn(new DeviceFlowRegistryImpl(DUMMY_VERSION.toJava(), dataBroker, DUMMY_NODE_II));
        lenient().when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceContext.getDeviceInfo()).thenReturn(mockedDeviceInfo);
        lenient().when(mockedDeviceContext.getMultiMsgCollector(Matchers.any())).thenReturn(multiMessageCollector);

        setup();
    }

    protected void setup() {
        //NOOP - to be overloaded
    }

    protected  <T> void mockSuccessfulFuture() {
        ListenableFuture<RpcResult<T>> dummySuccessfulFuture =
                Futures.immediateFuture(RpcResultBuilder.success((T) null).build());
        when(mockedRequestContext.getFuture()).thenReturn(dummySuccessfulFuture);
    }

    protected  <T> void mockSuccessfulFuture(final T result) {
        ListenableFuture<RpcResult<T>> dummySuccessfulFuture = Futures.immediateFuture(RpcResultBuilder.success(result)
                .build());
        when(mockedRequestContext.getFuture()).thenReturn(dummySuccessfulFuture);
    }

    protected ExperimenterMessageOfChoice mockExperimenter() {
        return new DummyExperimenter();
    }

    public class DummyExperimenter implements ExperimenterMessageOfChoice {
        @Override
        public Class<DummyExperimenter> implementedInterface() {
            return DummyExperimenter.class;
        }
    }
}
