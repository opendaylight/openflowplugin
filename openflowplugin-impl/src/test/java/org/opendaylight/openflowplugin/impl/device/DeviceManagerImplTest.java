/*
 *
 *  * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *
 */

package org.opendaylight.openflowplugin.impl.device;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueue;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandler;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.DeviceInitializationPhaseHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Capabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.CapabilitiesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.features.reply.PhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.role.service.rev150727.OfpRole;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerImplTest {

    private static final boolean TEST_VALUE_SWITCH_FEATURE_MANDATORY = true;
    private static final long TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA = 2000l;
    private static final KeyedInstanceIdentifier<Node, NodeKey> DUMMY_NODE_II = InstanceIdentifier.create(Nodes.class)
            .child(Node.class, new NodeKey(new NodeId("dummyNodeId")));
    private static final Short DUMMY_TABLE_ID = 1;
    private static final Long DUMMY_MAX_METER = 544L;
    private static final String DUMMY_DATAPATH_ID = "44";
    private static final Long DUMMY_PORT_NUMBER = 21L;

    @Mock
    CheckedFuture<Void, TransactionCommitFailedException> mockedFuture;
    @Mock
    private FeaturesReply mockFeatures;
    @Mock
    private OutboundQueue outboundQueueProvider;
    @Mock
    private DeviceInitializationPhaseHandler deviceInitPhaseHandler;
    @Mock
    private TranslatorLibrary translatorLibrary;
    @Mock
    private ConnectionContext mockConnectionContext;
    @Mock
    private ConnectionAdapter mockedConnectionAdapter;
    @Mock
    private DeviceContextImpl mockedDeviceContext;
    @Mock
    private NodeId mockedNodeId;

    @Before
    public void setUp() throws Exception {
        OpenflowPortsUtil.init();

        when(mockConnectionContext.getNodeId()).thenReturn(new NodeId("dummyNodeId"));
        when(mockConnectionContext.getFeatures()).thenReturn(mockFeatures);
        when(mockConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);
        when(mockedDeviceContext.getPrimaryConnectionContext()).thenReturn(mockConnectionContext);

        final Capabilities capabilitiesV13 = Mockito.mock(Capabilities.class);
        final CapabilitiesV10 capabilitiesV10 = Mockito.mock(CapabilitiesV10.class);
        when(mockFeatures.getCapabilities()).thenReturn(capabilitiesV13);
        when(mockFeatures.getCapabilitiesV10()).thenReturn(capabilitiesV10);
        when(mockFeatures.getDatapathId()).thenReturn(BigInteger.valueOf(21L));
    }

    @Test
    public void onDeviceContextLevelUpFailTest() {
        onDeviceContextLevelUp(true);
    }

    @Test
    public void onDeviceContextLevelUpSuccessTest() {
        onDeviceContextLevelUp(false);
    }

    private DeviceManagerImpl prepareDeviceManager() {
        return prepareDeviceManager(false);
    }

    private DeviceManagerImpl prepareDeviceManager(boolean withException) {
        DataBroker mockedDataBroker = mock(DataBroker.class);
        WriteTransaction mockedWriteTransaction = mock(WriteTransaction.class);

        BindingTransactionChain mockedTxChain = mock(BindingTransactionChain.class);
        WriteTransaction mockedWTx = mock(WriteTransaction.class);
        when(mockedTxChain.newWriteOnlyTransaction()).thenReturn(mockedWTx);
        when(mockedDataBroker.createTransactionChain(any(TransactionChainListener.class))).thenReturn
                (mockedTxChain);
        when(mockedDataBroker.newWriteOnlyTransaction()).thenReturn(mockedWriteTransaction);

        when(mockedWriteTransaction.submit()).thenReturn(mockedFuture);

        MessageIntelligenceAgency mockedMessageIntelligenceAgency = mock(MessageIntelligenceAgency.class);
        DeviceManagerImpl deviceManager = new DeviceManagerImpl(mockedDataBroker, mockedMessageIntelligenceAgency,
                TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA, false);
        deviceManager.setDeviceInitializationPhaseHandler(deviceInitPhaseHandler);

        return deviceManager;
    }

    public void onDeviceContextLevelUp(boolean withException) {
        DeviceManagerImpl deviceManager = prepareDeviceManager(withException);
        DeviceState mockedDeviceState = mock(DeviceState.class);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(mockedDeviceState.getRole()).thenReturn(OfpRole.BECOMEMASTER);

        if (withException) {
            doThrow(new IllegalStateException("dummy")).when(mockedDeviceContext).initialSubmitTransaction();
        }

        deviceManager.onDeviceContextLevelUp(mockedDeviceContext);
        if (withException) {
            verify(mockedDeviceContext).close();
        } else {
            verify(mockedDeviceContext).initialSubmitTransaction();
            verify(mockedDeviceContext).onPublished();
        }
    }

    @Test
    public void deviceConnectedTest() throws Exception{
        DeviceManagerImpl deviceManager = prepareDeviceManager();
        injectMockTranslatorLibrary(deviceManager);
        ConnectionContext mockConnectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_3);

        deviceManager.deviceConnected(mockConnectionContext);

        InOrder order = inOrder(mockConnectionContext);
        order.verify(mockConnectionContext).getFeatures();
        order.verify(mockConnectionContext).setOutboundQueueProvider(any(OutboundQueueProvider.class));
        order.verify(mockConnectionContext).setOutboundQueueHandleRegistration(
                Mockito.<OutboundQueueHandlerRegistration<OutboundQueueProvider>>any());
        order.verify(mockConnectionContext).getNodeId();
        Mockito.verify(deviceInitPhaseHandler).onDeviceContextLevelUp(Matchers.<DeviceContext>any());
    }

    @Test
    public void deviceConnectedV10Test() throws Exception{
        DeviceManagerImpl deviceManager = prepareDeviceManager();
        injectMockTranslatorLibrary(deviceManager);
        ConnectionContext mockConnectionContext = buildMockConnectionContext(OFConstants.OFP_VERSION_1_0);

        PhyPortBuilder phyPort = new PhyPortBuilder()
                .setPortNo(41L);
        when(mockFeatures.getPhyPort()).thenReturn(Collections.singletonList(phyPort.build()));
        MessageTranslator<Object, Object> mockedTranslator = Mockito.mock(MessageTranslator.class);
        when(mockedTranslator.translate(Matchers.<Object>any(), Matchers.<DeviceContext>any(), Matchers.any()))
                .thenReturn(null);
        when(translatorLibrary.lookupTranslator(Matchers.<TranslatorKey>any())).thenReturn(mockedTranslator);

        deviceManager.deviceConnected(mockConnectionContext);

        InOrder order = inOrder(mockConnectionContext);
        order.verify(mockConnectionContext).getFeatures();
        order.verify(mockConnectionContext).setOutboundQueueProvider(any(OutboundQueueProvider.class));
        order.verify(mockConnectionContext).setOutboundQueueHandleRegistration(
                Mockito.<OutboundQueueHandlerRegistration<OutboundQueueProvider>>any());
        order.verify(mockConnectionContext).getNodeId();
        Mockito.verify(deviceInitPhaseHandler).onDeviceContextLevelUp(Matchers.<DeviceContext>any());
    }

    protected ConnectionContext buildMockConnectionContext(short ofpVersion) {
        when(mockFeatures.getVersion()).thenReturn(ofpVersion);
        when(outboundQueueProvider.reserveEntry()).thenReturn(43L);
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                final FutureCallback<OfHeader> callBack = (FutureCallback<OfHeader>) invocation.getArguments()[2];
                callBack.onSuccess(null);
                return null;
            }
        })
                .when(outboundQueueProvider)
                .commitEntry(Matchers.anyLong(), Matchers.<MultipartRequestInput>any(), Matchers.<FutureCallback<OfHeader>>any());

        when(mockedConnectionAdapter.registerOutboundQueueHandler(Matchers.<OutboundQueueHandler>any(), Matchers.anyInt(), Matchers.anyLong()))
                .thenAnswer(new Answer<OutboundQueueHandlerRegistration<OutboundQueueHandler>>() {
                    @Override
                    public OutboundQueueHandlerRegistration<OutboundQueueHandler> answer(InvocationOnMock invocation) throws Throwable {
                        OutboundQueueHandler handler = (OutboundQueueHandler) invocation.getArguments()[0];
                        handler.onConnectionQueueChanged(outboundQueueProvider);
                        return null;
                    }
                });

        when(mockConnectionContext.getOutboundQueueProvider()).thenReturn(outboundQueueProvider);
        return mockConnectionContext;
    }

    private void injectMockTranslatorLibrary(DeviceManagerImpl deviceManager) {
        deviceManager.setTranslatorLibrary(translatorLibrary);
    }

    @Test
    public void testClose() throws Exception {
        DeviceContext deviceContext = Mockito.mock(DeviceContext.class);
        final DeviceManagerImpl deviceManager = prepareDeviceManager();
        final ConcurrentHashMap<NodeId, DeviceContext> deviceContexts = getContextsCollection(deviceManager);
        deviceContexts.put(mockedNodeId, deviceContext);
        Assert.assertEquals(1, deviceContexts.size());

        deviceManager.close();

        Mockito.verify(deviceContext).close();
    }

    private static ConcurrentHashMap<NodeId, DeviceContext> getContextsCollection(DeviceManagerImpl deviceManager) throws NoSuchFieldException, IllegalAccessException {
        // HACK: contexts collection for testing shall be accessed in some more civilized way
        final Field contextsField = DeviceManagerImpl.class.getDeclaredField("deviceContexts");
        Assert.assertNotNull(contextsField);
        contextsField.setAccessible(true);
        return (ConcurrentHashMap<NodeId, DeviceContext>) contextsField.get(deviceManager);
    }

}
