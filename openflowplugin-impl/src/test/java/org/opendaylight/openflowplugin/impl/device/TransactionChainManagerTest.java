/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import io.netty.util.HashedWheelTimer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

/**
 * Created by mirehak on 4/5/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionChainManagerTest {

    @Mock
    private DataBroker dataBroker;
    @Mock
    private ConnectionContext connectionContext;
    @Mock
    private BindingTransactionChain txChain;
    @Mock
    private WriteTransaction writeTx;
    @Mock
    private TransactionChain<?, ?> transactionChain;
    @Mock
    HashedWheelTimer timer;
    @Mock
    Registration registration;
    @Mock
    DeviceState deviceState;

    @Mock
    private KeyedInstanceIdentifier<Node, NodeKey> nodeKeyIdent;

    private TransactionChainManager txChainManager;
    private InstanceIdentifier<Node> path;
    private NodeId nodeId;

    @Before
    public void setUp() throws Exception {
        final ReadOnlyTransaction readOnlyTx = Mockito.mock(ReadOnlyTransaction.class);
        final CheckedFuture<Optional<Node>, ReadFailedException> noExistNodeFuture = Futures.immediateCheckedFuture(Optional.<Node>absent());
        Mockito.when(readOnlyTx.read(LogicalDatastoreType.OPERATIONAL, nodeKeyIdent)).thenReturn(noExistNodeFuture);
        Mockito.when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTx);
        Mockito.when(dataBroker.createTransactionChain(Matchers.any(TransactionChainListener.class)))
                .thenReturn(txChain);
        nodeId = new NodeId("h2g2:42");
        nodeKeyIdent = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
        Mockito.when(deviceState.getNodeInstanceIdentifier()).thenReturn(nodeKeyIdent);
        Mockito.when(deviceState.getNodeId()).thenReturn(nodeId);
        txChainManager = new TransactionChainManager(dataBroker, deviceState);
        Mockito.when(txChain.newWriteOnlyTransaction()).thenReturn(writeTx);

        path = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
        Mockito.when(writeTx.submit()).thenReturn(Futures.<Void, TransactionCommitFailedException>immediateCheckedFuture(null));
        txChainManager.activateTransactionManager();
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(txChain, writeTx);
    }

    @Test
    public void testWriteToTransaction() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
    }

    /**
     * FIXME: Need to change the test on behalf the clustering transaction chain manager changes
     * @throws Exception
     */
    @Ignore
    @Test
    public void testSubmitTransaction() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.enableSubmit();
        txChainManager.activateTransactionManager();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.activateTransactionManager();
        txChainManager.submitWriteTransaction();

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data);
        Mockito.verify(writeTx).submit();
    }

    /**
     * test of {@link TransactionChainManager#enableSubmit()}: no submit - counter is not active
     *
     * @throws Exception
     */
    @Test
    public void testEnableCounter1() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx, Mockito.times(2)).put(LogicalDatastoreType.CONFIGURATION, path, data);
        Mockito.verify(writeTx, Mockito.never()).submit();
    }

    /**
     * FIXME: Need to change the test on behalf the clustering transaction chain manager changes
     * @throws Exception
     */
    @Ignore
    @Test
    public void testOnTransactionChainFailed() throws Exception {
        txChainManager.onTransactionChainFailed(transactionChain, Mockito.mock(AsyncTransaction.class), Mockito.mock(Throwable.class));
        Mockito.verify(dataBroker, Mockito.times(2)).createTransactionChain(txChainManager);
    }

    @Test
    public void testOnTransactionChainSuccessful() throws Exception {
        txChainManager.onTransactionChainSuccessful(transactionChain);
        // NOOP
        Mockito.verifyZeroInteractions(transactionChain);
    }

    @Test
    public void testAddDeleteOperationTotTxChain() throws Exception {
        txChainManager.addDeleteOperationTotTxChain(LogicalDatastoreType.CONFIGURATION, path);

        Mockito.verify(txChain).newWriteOnlyTransaction();
        Mockito.verify(writeTx).delete(LogicalDatastoreType.CONFIGURATION, path);
    }
}