/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.device;

import static org.mockito.ArgumentMatchers.any;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.binding.api.ReadWriteTransaction;
import org.opendaylight.mdsal.binding.api.Transaction;
import org.opendaylight.mdsal.binding.api.TransactionChain;
import org.opendaylight.mdsal.binding.api.TransactionChainListener;
import org.opendaylight.mdsal.common.api.CommitInfo;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.common.api.TransactionCommitFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.common.txchain.TransactionChainManager;
import org.opendaylight.openflowplugin.impl.util.DeviceStateUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class TransactionChainManagerTest {

    @Mock
    private DataBroker dataBroker;
    @Mock
    private TransactionChain txChain;
    @Mock
    private ReadWriteTransaction writeTx;
    @Mock
    private TransactionChain transactionChain;
    @Mock
    DeviceInfo deviceInfo;

    @Mock
    private KeyedInstanceIdentifier<Node, NodeKey> nodeKeyIdent;

    private TransactionChainManager txChainManager;
    private InstanceIdentifier<Node> path;
    private NodeId nodeId;

    @Before
    public void setUp() throws Exception {
        final ReadTransaction readOnlyTx = Mockito.mock(ReadTransaction.class);
        Mockito.when(dataBroker.createTransactionChain(any(TransactionChainListener.class)))
                .thenReturn(txChain);
        nodeId = new NodeId("h2g2:42");
        nodeKeyIdent = DeviceStateUtil.createNodeInstanceIdentifier(nodeId);
        txChainManager = new TransactionChainManager(dataBroker, nodeId.getValue());
        Mockito.when(txChain.newReadWriteTransaction()).thenReturn(writeTx);

        path = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId));
        Mockito.doReturn(CommitInfo.emptyFluentFuture()).when(writeTx).commit();
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
     * Tests transaction submit {@link TransactionChainManager#submitTransaction()}.
     */
    @Test
    public void testSubmitTransaction() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx).commit();
    }

    /**
     * test of {@link TransactionChainManager#submitTransaction()}: no submit, never enabled.
     */
    @Test
    public void testSubmitTransaction1() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx, Mockito.never()).commit();
    }

    @Test
    public void testSubmitTransactionFailed() throws Exception {
        Mockito.doReturn(FluentFutures.immediateFailedFluentFuture(new TransactionCommitFailedException("mock")))
            .when(writeTx).commit();
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.submitTransaction();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx).commit();
    }

    /**
     * Test of {@link TransactionChainManager#enableSubmit()}: no submit - counter is not active.
     */
    @Test
    public void testEnableCounter1() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx, Mockito.times(2)).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx, Mockito.never()).commit();
    }

    @Test
    public void testOnTransactionChainFailed() throws Exception {
        txChainManager.onTransactionChainFailed(txChain, Mockito.mock(Transaction.class),
            Mockito.mock(Throwable.class));
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

    @Test
    public void testDeactivateTransactionChainManagerFailed() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);

        txChainManager.deactivateTransactionManager();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx, Mockito.never()).commit();
        Mockito.verify(writeTx).cancel();
        Mockito.verify(txChain).close();
    }

    @Test
    public void testShuttingDown() throws Exception {
        final Node data = new NodeBuilder().setId(nodeId).build();
        txChainManager.initialSubmitWriteTransaction();
        txChainManager.writeToTransaction(LogicalDatastoreType.CONFIGURATION, path, data, false);
        txChainManager.shuttingDown();

        Mockito.verify(txChain).newReadWriteTransaction();
        Mockito.verify(writeTx).put(LogicalDatastoreType.CONFIGURATION, path, data, false);
        Mockito.verify(writeTx).commit();
    }

    @Test
    public void testClose() {
        txChainManager.shuttingDown();
        txChainManager.close();
        Mockito.verify(txChain).close();
    }
}
