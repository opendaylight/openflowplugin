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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.BindingTransactionChain;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChain;
import org.opendaylight.controller.md.sal.common.api.data.TransactionChainListener;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
    private BindingTransactionChain txChain;
    @Mock
    private ReadWriteTransaction writeTx;
    @Mock
    private TransactionChain<?, ?> transactionChain;
    @Mock
    DeviceInfo deviceInfo;

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
        Mockito.when(deviceInfo.getNodeInstanceIdentifier()).thenReturn(nodeKeyIdent);
        Mockito.when(deviceInfo.getNodeId()).thenReturn(nodeId);
        txChainManager = new TransactionChainManager(dataBroker, nodeId.getValue());
        Mockito.when(txChain.newReadWriteTransaction()).thenReturn(writeTx);

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
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
    }

    /**
     * test of {@link TransactionChainManager#submitTransaction()}
     * @throws Exception
     */
    @Test
    public void testSubmitTransaction() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx).submit();
    }

    /**
     * test of {@link TransactionChainManager#submitTransaction()}: no submit, never enabled
     * @throws Exception
     */
    @Test
    public void testSubmitTransaction1() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx, Mockito.never()).submit();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testSubmitTransactionFailed() throws Exception {
        Mockito.when(writeTx.submit()).thenReturn(Futures.<Void, TransactionCommitFailedException>immediateFailedCheckedFuture(new TransactionCommitFailedException("mock")));
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
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
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx, Mockito.times(2)).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx, Mockito.never()).submit();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testOnTransactionChainFailed() throws Exception {
        txChainManager.onTransactionChainFailed(txChain, Mockito.mock(AsyncTransaction.class), Mockito.mock(Throwable.class));
        Mockito.verify(txChain).close();
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
        txChainManager.addDeleteOperationToTxChain(LogicalDatastoreType.CONFIGURATION, path);

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).delete(LogicalDatastoreType.CONFIGURATION, path);
    }

    @Test
    public void testDeactivateTransactionChainManager() throws Exception {
        txChainManager.deactivateTransactionManager();

        Mockito.verify(txChain).close();
    }

    /**
     * @throws Exception
     */
    @Test
    public void testDeactivateTransactionChainManagerFailed() throws Exception {
        Mockito.when(writeTx.submit()).thenReturn(Futures.<Void, TransactionCommitFailedException>immediateFailedCheckedFuture(new TransactionCommitFailedException("mock")));
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        txChainManager.deactivateTransactionManager();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx, Mockito.never()).submit();
        Mockito.verify(writeTx).cancel();
        Mockito.verify(txChain).close();
    }

    @Test
    public void testShuttingDown() throws Exception{
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.shuttingDown();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx).submit();
    }

    @Test
    public void testClose() {
        txChainManager.shuttingDown();
        txChainManager.close();
        Mockito.verify(txChain).close();
    }
}
