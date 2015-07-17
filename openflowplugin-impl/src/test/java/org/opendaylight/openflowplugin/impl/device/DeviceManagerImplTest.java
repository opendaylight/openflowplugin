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
import static org.mockito.Mockito.*;

import com.google.common.util.concurrent.CheckedFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.Futures;
import com.sun.xml.internal.stream.buffer.sax.Features;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueHandlerRegistration;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.connection.OutboundQueueProvider;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;
import org.opendaylight.openflowplugin.impl.device.DeviceManagerImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;

@RunWith(MockitoJUnitRunner.class)
public class DeviceManagerImplTest {

    private static final boolean TEST_VALUE_SWITCH_FEATURE_MANDATORY = true;
    private static final long TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA = 2000l;

    @Mock
    CheckedFuture<Void,TransactionCommitFailedException> mockedFuture;

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
        DeviceManagerImpl deviceManager = new DeviceManagerImpl(mockedDataBroker, mockedMessageIntelligenceAgency, TEST_VALUE_SWITCH_FEATURE_MANDATORY,
                TEST_VALUE_GLOBAL_NOTIFICATION_QUOTA);

        return deviceManager;
    }

    public void onDeviceContextLevelUp(boolean withException) {
        DeviceManagerImpl deviceManager = prepareDeviceManager(withException);

        DeviceContextImpl mockedDeviceContext = mock(DeviceContextImpl.class);
        if (withException) {
            doThrow(new IllegalStateException("dummy")).when(mockedDeviceContext).initialSubmitTransaction();
            DeviceState mockedDeviceState = mock(DeviceState.class);
            when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
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
    public void deviceConnectedTest() {
    }

    @Test
    public void deviceConnectedNewConnectionTest() {
        DeviceManagerImpl deviceManager = prepareDeviceManager();
        TranslatorLibrary mockedTraslatorLibrary = mock(TranslatorLibrary.class);
        mockedTraslatorLibrary.lookupTranslator(any(TranslatorKey.class));

        deviceManager.setTranslatorLibrary(mockedTraslatorLibrary);

        ConnectionContext mockConnectionContext = mock(ConnectionContext.class);
        when(mockConnectionContext.getNodeId()).thenReturn(new NodeId("dummyNodeId"));


        FeaturesReply mockFeatures = mock(FeaturesReply.class);
        when(mockConnectionContext.getFeatures()).thenReturn(mockFeatures);


        ConnectionAdapter mockedConnectionAdapter = mock(ConnectionAdapter.class);
        when(mockConnectionContext.getConnectionAdapter()).thenReturn(mockedConnectionAdapter);

        deviceManager.deviceConnected(mockConnectionContext);

        InOrder order = inOrder(mockConnectionContext);
        order.verify(mockConnectionContext).getFeatures();
        order.verify(mockConnectionContext).setOutboundQueueProvider(any(OutboundQueueProvider.class));
        order.verify(mockConnectionContext).setOutboundQueueHandleRegistration(any(OutboundQueueHandlerRegistration
                .class));
        order.verify(mockConnectionContext).getNodeId();
        order.verify(mockConnectionContext).setDeviceDisconnectedHandler(any(DeviceContext.class));
    }

    @Test
    public void chainTableTrunkWriteOF10Test() {
        DeviceContext mockedDeviceContext = mock(DeviceContext.class);
        DeviceState mockedDeviceState = mock(DeviceState.class);

        KeyedInstanceIdentifier<Node, NodeKey> dummyNodeII = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId("dummyNodeId")));

        GetFeaturesOutput mockedFeatures = mock(GetFeaturesOutput.class);
        when(mockedFeatures.getTables()).thenReturn((short)2);
        when(mockedDeviceState.getFeatures()).thenReturn(mockedFeatures);


        when(mockedDeviceState.getNodeInstanceIdentifier()).thenReturn(dummyNodeII);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        RpcResult<List<MultipartReply>> mockedRpcResult = mock(RpcResult.class);
        when(mockedRpcResult.isSuccessful()).thenReturn(true);
        List<RpcResult<List<MultipartReply>>> data = new ArrayList<RpcResult<List<MultipartReply>>>();
        data.add(mockedRpcResult);
        data.add(mockedRpcResult);

        DeviceManagerImpl.chainTableTrunkWriteOF10(mockedDeviceContext, Futures.immediateFuture(data));
        verify(mockedDeviceContext,times(3))
                .writeToTransaction(any(LogicalDatastoreType.class), any(InstanceIdentifier.class), any(FlowCapableNode.class));
    }

}
